package fei.upce.nnpro.remax.realestates;

import fei.upce.nnpro.remax.address.entity.Address;
import fei.upce.nnpro.remax.address.repository.AddressRepository;
import fei.upce.nnpro.remax.address.service.AddressService;
import fei.upce.nnpro.remax.images.entity.Image;
import fei.upce.nnpro.remax.images.repository.ImageRepository;
import fei.upce.nnpro.remax.realestates.dto.RealEstateDto;
import fei.upce.nnpro.remax.realestates.dto.RealEstateFilterDto;
import fei.upce.nnpro.remax.realestates.dto.RealEstateMapper;
import fei.upce.nnpro.remax.realestates.entity.Apartment;
import fei.upce.nnpro.remax.realestates.entity.House;
import fei.upce.nnpro.remax.realestates.entity.Land;
import fei.upce.nnpro.remax.realestates.entity.PriceHistory;
import fei.upce.nnpro.remax.realestates.entity.RealEstate;
import fei.upce.nnpro.remax.realestates.repository.RealEstateRepository;
import fei.upce.nnpro.remax.realestates.service.RealEstateService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RealEstateServiceTest {

    @Mock
    private RealEstateRepository realEstateRepository;

    @Mock
    private AddressService addressService;

    @Mock
    private AddressRepository addressRepository;

    @Mock
    private RealEstateMapper realEstateMapper;

    @Mock
    private ImageRepository imageRepository;

    @InjectMocks
    private RealEstateService realEstateService;

    // --------------------------------------------------------------------------------------
    // CREATE TESTS
    // --------------------------------------------------------------------------------------

    @Test
    @DisplayName("Create: Should map entity, save address, init price history, and save entity")
    void createRealEstate_Success() {
        // Arrange
        RealEstateDto dto = new RealEstateDto();
        dto.setPrice(5000000.0);
        Address address = new Address();
        dto.setAddress(address);

        RealEstate mappedEntity = new Apartment();
        mappedEntity.setAddress(address);
        // The mapper returns an entity. The service is responsible for creating the history list if null.
        mappedEntity.setPriceHistory(null);

        when(realEstateMapper.toEntity(dto)).thenReturn(mappedEntity);
        when(realEstateRepository.save(any(RealEstate.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(addressService.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RealEstate result = realEstateService.createRealEstate(dto);

        // Assert
        verify(addressService).save(address);
        verify(realEstateRepository).save(mappedEntity);

        // Verify Price History initialization
        assertThat(result.getPriceHistory()).isNotNull().hasSize(1);
        assertThat(result.getPriceHistory().getFirst().getPrice()).isEqualTo(5000000.0);
        assertThat(result.getPriceHistory().getFirst().getTimestamp()).isNotNull();
    }

    // --------------------------------------------------------------------------------------
    // UPDATE TESTS
    // --------------------------------------------------------------------------------------

    @Test
    @DisplayName("Update: Should only update fields present in DTO (Partial Update)")
    void updateRealEstate_PartialUpdate() {
        // Arrange
        Long id = 1L;
        String oldName = "Old Name";
        String oldDescription = "Old Description";
        String newName = "New Name";

        // Existing Entity
        Apartment existing = new Apartment();
        existing.setId(id);
        existing.setName(oldName);
        existing.setDescription(oldDescription);
        existing.setPriceHistory(new ArrayList<>()); // Empty history

        // DTO with only Name changed, Description is null
        RealEstateDto dto = new RealEstateDto();
        dto.setName(newName);
        dto.setDescription(null); // Should not overwrite existing

        when(realEstateRepository.findById(id)).thenReturn(Optional.of(existing));
        when(realEstateRepository.save(existing)).thenReturn(existing);

        // Act
        RealEstate result = realEstateService.updateRealEstate(id, dto);

        // Assert
        // Verify mapper was NOT called (logic is now in service)
        verify(realEstateMapper, never()).toEntity(any());

        // Verify fields
        assertThat(result.getName()).isEqualTo(newName); // Updated
        assertThat(result.getDescription()).isEqualTo(oldDescription); // Preserved

        verify(realEstateRepository).save(existing);
    }

    @Test
    @DisplayName("Update: Should call AddressService update if address is provided")
    void updateRealEstate_AddressUpdate() {
        // Arrange
        Long id = 1L;
        Apartment existing = new Apartment();
        existing.setId(id);
        Address existingAddr = new Address();
        existing.setAddress(existingAddr);

        RealEstateDto dto = new RealEstateDto();
        Address newAddrData = new Address();
        dto.setAddress(newAddrData);

        when(realEstateRepository.findById(id)).thenReturn(Optional.of(existing));
        when(realEstateRepository.save(existing)).thenReturn(existing);

        // Act
        realEstateService.updateRealEstate(id, dto);

        // Assert
        verify(addressService).update(newAddrData, existingAddr);
    }

    @Test
    @DisplayName("Update: Should add new price history entry ONLY if price changes")
    void updateRealEstate_PriceChange() {
        // Arrange
        Long id = 1L;
        double oldPrice = 1000.0;
        double newPrice = 2000.0;

        // Existing
        RealEstate existing = new Apartment();
        existing.setId(id);
        PriceHistory ph = new PriceHistory();
        ph.setPrice(oldPrice);
        existing.setPriceHistory(new ArrayList<>(List.of(ph))); // Mutable list

        // DTO
        RealEstateDto dto = new RealEstateDto();
        dto.setPrice(newPrice);

        when(realEstateRepository.findById(id)).thenReturn(Optional.of(existing));
        when(realEstateRepository.save(existing)).thenReturn(existing);

        // Act
        RealEstate result = realEstateService.updateRealEstate(id, dto);

        // Assert
        assertThat(result.getPriceHistory()).hasSize(2);
        assertThat(result.getPriceHistory().getLast().getPrice()).isEqualTo(newPrice);
    }

    @Test
    @DisplayName("Update: Should NOT add price history if price matches current")
    void updateRealEstate_NoPriceChange() {
        // Arrange
        Long id = 1L;
        double currentPrice = 1000.0;

        RealEstate existing = new Apartment();
        existing.setId(id);
        PriceHistory ph = new PriceHistory();
        ph.setPrice(currentPrice);
        existing.setPriceHistory(new ArrayList<>(List.of(ph)));

        RealEstateDto dto = new RealEstateDto();
        dto.setPrice(currentPrice); // Same price

        when(realEstateRepository.findById(id)).thenReturn(Optional.of(existing));
        when(realEstateRepository.save(existing)).thenReturn(existing);

        // Act
        RealEstate result = realEstateService.updateRealEstate(id, dto);

        // Assert
        assertThat(result.getPriceHistory()).hasSize(1);
    }

    @Test
    @DisplayName("Update: Should update specific subclass fields (Apartment)")
    void updateRealEstate_SubclassFields() {
        // Arrange
        Long id = 1L;
        Apartment existing = new Apartment();
        existing.setId(id);
        existing.setFloor(1);
        existing.setElevator(false);

        RealEstateDto dto = new RealEstateDto();
        dto.setFloor(5);
        dto.setElevator(true);
        // ownershipType is null in DTO, should not crash or overwrite

        when(realEstateRepository.findById(id)).thenReturn(Optional.of(existing));
        when(realEstateRepository.save(existing)).thenReturn(existing);

        // Act
        RealEstate result = realEstateService.updateRealEstate(id, dto);

        // Assert
        assertThat(result).isInstanceOf(Apartment.class);
        Apartment apt = (Apartment) result;
        assertThat(apt.getFloor()).isEqualTo(5);
        assertThat(apt.isElevator()).isTrue();
    }

    @Test
    @DisplayName("Update: Should update images when IDs are provided")
    void updateRealEstate_Images() {
        // Arrange
        Long id = 1L;
        RealEstate existing = new Apartment();
        existing.setId(id);
        existing.setImage(new ArrayList<>());

        List<Long> imageIds = List.of(100L, 101L);
        RealEstateDto dto = new RealEstateDto();
        dto.setImageIds(imageIds);

        List<Image> mockImages = List.of(new Image(), new Image());
        when(imageRepository.findAllById(imageIds)).thenReturn(mockImages);
        when(realEstateRepository.findById(id)).thenReturn(Optional.of(existing));
        when(realEstateRepository.save(existing)).thenReturn(existing);

        // Act
        RealEstate result = realEstateService.updateRealEstate(id, dto);

        // Assert
        assertThat(result.getImage()).hasSize(2);
        verify(imageRepository).findAllById(imageIds);
    }

    @Test
    @DisplayName("Update: Should throw exception if ID not found")
    void updateRealEstate_NotFound() {
        // Arrange
        Long id = 99L;
        RealEstateDto dto = new RealEstateDto();
        when(realEstateRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> realEstateService.updateRealEstate(id, dto))
                .isInstanceOf(EntityNotFoundException.class);

        verify(realEstateRepository, never()).save(any());
    }

    // --------------------------------------------------------------------------------------
    // GET & SEARCH TESTS
    // --------------------------------------------------------------------------------------

    @Test
    @DisplayName("Get: Should return entity when found")
    void getRealEstate_Found() {
        // Arrange
        Long id = 1L;
        RealEstate estate = new Apartment();
        estate.setId(id);
        when(realEstateRepository.findById(id)).thenReturn(Optional.of(estate));

        // Act
        RealEstate result = realEstateService.getRealEstate(id);

        // Assert
        assertThat(result).isEqualTo(estate);
    }

    @Test
    @DisplayName("Get: Should throw exception when not found")
    void getRealEstate_NotFound() {
        // Arrange
        Long id = 99L;
        when(realEstateRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> realEstateService.getRealEstate(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Search: Should invoke repository findAll with specification and pageable")
    void searchRealEstates() {
        // Arrange
        RealEstateFilterDto filter = new RealEstateFilterDto();
        filter.setCity("Prague");

        Pageable pageable = PageRequest.of(0, 10);
        List<RealEstate> list = List.of(new Apartment(), new Apartment());
        Page<RealEstate> expectedPage = new PageImpl<>(list, pageable, list.size());

        when(realEstateRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(expectedPage);

        // Act
        Page<RealEstate> result = realEstateService.searchRealEstates(filter, pageable);

        // Assert
        assertThat(result).hasSize(2);
        verify(realEstateRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    // --------------------------------------------------------------------------------------
    // NEW COMPREHENSIVE TESTS FOR IMPROVED COVERAGE
    // --------------------------------------------------------------------------------------

    @Test
    @DisplayName("Create: Should handle null address without error")
    void createRealEstate_NullAddress() {
        // Arrange
        RealEstateDto dto = new RealEstateDto();
        dto.setPrice(5000000.0);
        dto.setAddress(null);

        RealEstate mappedEntity = new Apartment();
        mappedEntity.setAddress(null);
        mappedEntity.setPriceHistory(null);

        when(realEstateMapper.toEntity(dto)).thenReturn(mappedEntity);
        when(realEstateRepository.save(any(RealEstate.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RealEstate result = realEstateService.createRealEstate(dto);

        // Assert
        verify(addressService, never()).save(any());
        verify(realEstateRepository).save(mappedEntity);
        assertThat(result.getPriceHistory()).isNotNull().hasSize(1);
    }

    @Test
    @DisplayName("Create: Should handle null price without initializing price history")
    void createRealEstate_NullPrice() {
        // Arrange
        RealEstateDto dto = new RealEstateDto();
        dto.setPrice(null);
        Address address = new Address();
        dto.setAddress(address);

        RealEstate mappedEntity = new Apartment();
        mappedEntity.setAddress(address);
        mappedEntity.setPriceHistory(null);

        when(realEstateMapper.toEntity(dto)).thenReturn(mappedEntity);
        when(realEstateRepository.save(any(RealEstate.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(addressService.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        RealEstate result = realEstateService.createRealEstate(dto);

        // Assert
        verify(addressService).save(address);
        verify(realEstateRepository).save(mappedEntity);
        assertThat(result.getPriceHistory()).isNull();
    }

    @Test
    @DisplayName("Update: Should handle null address in DTO without updating")
    void updateRealEstate_NullAddressInDto() {
        // Arrange
        Long id = 1L;
        Apartment existing = new Apartment();
        existing.setId(id);
        Address existingAddr = new Address();
        existing.setAddress(existingAddr);

        RealEstateDto dto = new RealEstateDto();
        dto.setAddress(null);

        when(realEstateRepository.findById(id)).thenReturn(Optional.of(existing));
        when(realEstateRepository.save(existing)).thenReturn(existing);

        // Act
        realEstateService.updateRealEstate(id, dto);

        // Assert
        verify(addressService, never()).update(any(), any());
    }

    @Test
    @DisplayName("Update: Should handle null address in existing entity")
    void updateRealEstate_NullAddressInExisting() {
        // Arrange
        Long id = 1L;
        Apartment existing = new Apartment();
        existing.setId(id);
        existing.setAddress(null);

        RealEstateDto dto = new RealEstateDto();
        Address newAddrData = new Address();
        dto.setAddress(newAddrData);

        when(realEstateRepository.findById(id)).thenReturn(Optional.of(existing));
        when(realEstateRepository.save(existing)).thenReturn(existing);

        // Act
        realEstateService.updateRealEstate(id, dto);

        // Assert
        verify(addressService, never()).update(any(), any());
    }

    @Test
    @DisplayName("Update: Should initialize price history if null when price provided")
    void updateRealEstate_NullPriceHistoryInit() {
        // Arrange
        Long id = 1L;
        RealEstate existing = new Apartment();
        existing.setId(id);
        existing.setPriceHistory(null);

        RealEstateDto dto = new RealEstateDto();
        dto.setPrice(3000.0);

        when(realEstateRepository.findById(id)).thenReturn(Optional.of(existing));
        when(realEstateRepository.save(existing)).thenReturn(existing);

        // Act
        RealEstate result = realEstateService.updateRealEstate(id, dto);

        // Assert
        assertThat(result.getPriceHistory()).isNotNull().hasSize(1);
        assertThat(result.getPriceHistory().getFirst().getPrice()).isEqualTo(3000.0);
    }

    @Test
    @DisplayName("Update: Should add price history when existing history is empty")
    void updateRealEstate_EmptyPriceHistory() {
        // Arrange
        Long id = 1L;
        RealEstate existing = new Apartment();
        existing.setId(id);
        existing.setPriceHistory(new ArrayList<>());

        RealEstateDto dto = new RealEstateDto();
        dto.setPrice(5000.0);

        when(realEstateRepository.findById(id)).thenReturn(Optional.of(existing));
        when(realEstateRepository.save(existing)).thenReturn(existing);

        // Act
        RealEstate result = realEstateService.updateRealEstate(id, dto);

        // Assert
        assertThat(result.getPriceHistory()).hasSize(1);
        assertThat(result.getPriceHistory().getFirst().getPrice()).isEqualTo(5000.0);
    }

    @Test
    @DisplayName("Update: Should clear images when empty list provided")
    void updateRealEstate_EmptyImageList() {
        // Arrange
        Long id = 1L;
        RealEstate existing = new Apartment();
        existing.setId(id);
        existing.setImage(new ArrayList<>(List.of(new Image(), new Image())));

        RealEstateDto dto = new RealEstateDto();
        dto.setImageIds(new ArrayList<>());

        when(realEstateRepository.findById(id)).thenReturn(Optional.of(existing));
        when(realEstateRepository.save(existing)).thenReturn(existing);

        // Act
        RealEstate result = realEstateService.updateRealEstate(id, dto);

        // Assert
        assertThat(result.getImage()).isEmpty();
        verify(imageRepository, never()).findAllById(any());
    }

    @Test
    @DisplayName("Update: Should not update images when imageIds is null")
    void updateRealEstate_NullImageIds() {
        // Arrange
        Long id = 1L;
        RealEstate existing = new Apartment();
        existing.setId(id);
        List<Image> originalImages = new ArrayList<>(List.of(new Image(), new Image()));
        existing.setImage(originalImages);

        RealEstateDto dto = new RealEstateDto();
        dto.setImageIds(null);

        when(realEstateRepository.findById(id)).thenReturn(Optional.of(existing));
        when(realEstateRepository.save(existing)).thenReturn(existing);

        // Act
        RealEstate result = realEstateService.updateRealEstate(id, dto);

        // Assert
        assertThat(result.getImage()).hasSize(2);
        verify(imageRepository, never()).findAllById(any());
    }

    @Test
    @DisplayName("Update: Should update House-specific fields")
    void updateRealEstate_HouseSubclassFields() {
        // Arrange
        Long id = 1L;
        House existing = new House();
        existing.setId(id);
        existing.setPlotArea(500.0);
        existing.setStories(1);

        RealEstateDto dto = new RealEstateDto();
        dto.setPlotArea(750.0);
        dto.setStories(2);
        dto.setHouseType(fei.upce.nnpro.remax.realestates.entity.enums.HouseType.DETACHED);

        when(realEstateRepository.findById(id)).thenReturn(Optional.of(existing));
        when(realEstateRepository.save(existing)).thenReturn(existing);

        // Act
        RealEstate result = realEstateService.updateRealEstate(id, dto);

        // Assert
        assertThat(result).isInstanceOf(House.class);
        House house = (House) result;
        assertThat(house.getPlotArea()).isEqualTo(750.0);
        assertThat(house.getStories()).isEqualTo(2);
        assertThat(house.getHouseType()).isEqualTo(fei.upce.nnpro.remax.realestates.entity.enums.HouseType.DETACHED);
    }

    @Test
    @DisplayName("Update: Should update Land-specific fields")
    void updateRealEstate_LandSubclassFields() {
        // Arrange
        Long id = 1L;
        Land existing = new Land();
        existing.setId(id);
        existing.setForHousing(false);

        RealEstateDto dto = new RealEstateDto();
        dto.setIsForHousing(true);

        when(realEstateRepository.findById(id)).thenReturn(Optional.of(existing));
        when(realEstateRepository.save(existing)).thenReturn(existing);

        // Act
        RealEstate result = realEstateService.updateRealEstate(id, dto);

        // Assert
        assertThat(result).isInstanceOf(Land.class);
        Land land = (Land) result;
        assertThat(land.isForHousing()).isTrue();
    }

    @Test
    @DisplayName("Update: Should not update House fields when null in DTO")
    void updateRealEstate_HouseFieldsNull() {
        // Arrange
        Long id = 1L;
        House existing = new House();
        existing.setId(id);
        existing.setPlotArea(500.0);
        existing.setStories(1);
        existing.setHouseType(fei.upce.nnpro.remax.realestates.entity.enums.HouseType.SEMI_DETACHED);

        RealEstateDto dto = new RealEstateDto();
        dto.setPlotArea(null);
        dto.setStories(null);
        dto.setHouseType(null);

        when(realEstateRepository.findById(id)).thenReturn(Optional.of(existing));
        when(realEstateRepository.save(existing)).thenReturn(existing);

        // Act
        RealEstate result = realEstateService.updateRealEstate(id, dto);

        // Assert
        House house = (House) result;
        assertThat(house.getPlotArea()).isEqualTo(500.0);
        assertThat(house.getStories()).isEqualTo(1);
        assertThat(house.getHouseType()).isEqualTo(fei.upce.nnpro.remax.realestates.entity.enums.HouseType.SEMI_DETACHED);
    }

    @Test
    @DisplayName("Update: Should not update Land fields when null in DTO")
    void updateRealEstate_LandFieldsNull() {
        // Arrange
        Long id = 1L;
        Land existing = new Land();
        existing.setId(id);
        existing.setForHousing(true);

        RealEstateDto dto = new RealEstateDto();
        dto.setIsForHousing(null);

        when(realEstateRepository.findById(id)).thenReturn(Optional.of(existing));
        when(realEstateRepository.save(existing)).thenReturn(existing);

        // Act
        RealEstate result = realEstateService.updateRealEstate(id, dto);

        // Assert
        Land land = (Land) result;
        assertThat(land.isForHousing()).isTrue();
    }

    @Test
    @DisplayName("Update: Should update all Apartment fields when provided")
    void updateRealEstate_AllApartmentFields() {
        // Arrange
        Long id = 1L;
        Apartment existing = new Apartment();
        existing.setId(id);

        RealEstateDto dto = new RealEstateDto();
        dto.setOwnershipType(fei.upce.nnpro.remax.realestates.entity.enums.ApartmentOwnershipType.COOPERATIVE_OWNERSHIP);
        dto.setFloor(10);
        dto.setTotalFloors(15);
        dto.setElevator(true);
        dto.setBalcony(true);
        dto.setRooms(4);

        when(realEstateRepository.findById(id)).thenReturn(Optional.of(existing));
        when(realEstateRepository.save(existing)).thenReturn(existing);

        // Act
        RealEstate result = realEstateService.updateRealEstate(id, dto);

        // Assert
        Apartment apt = (Apartment) result;
        assertThat(apt.getOwnershipType()).isEqualTo(fei.upce.nnpro.remax.realestates.entity.enums.ApartmentOwnershipType.COOPERATIVE_OWNERSHIP);
        assertThat(apt.getFloor()).isEqualTo(10);
        assertThat(apt.getTotalFloors()).isEqualTo(15);
        assertThat(apt.isElevator()).isTrue();
        assertThat(apt.isBalcony()).isTrue();
        assertThat(apt.getRooms()).isEqualTo(4);
    }

    @Test
    @DisplayName("Update: Should update all common fields when provided")
    void updateRealEstate_AllCommonFields() {
        // Arrange
        Long id = 1L;
        Apartment existing = new Apartment();
        existing.setId(id);

        RealEstateDto dto = new RealEstateDto();
        dto.setName("Updated Name");
        dto.setDescription("Updated Description");
        dto.setStatus(fei.upce.nnpro.remax.realestates.entity.enums.Status.BOUGHT);
        dto.setUsableArea(120.5);
        dto.setContractType(fei.upce.nnpro.remax.realestates.entity.enums.ContractType.RENTAL);
        dto.setPriceDisclosure(fei.upce.nnpro.remax.realestates.entity.enums.PriceDisclosure.ASK);
        dto.setCommission(fei.upce.nnpro.remax.realestates.entity.enums.Commission.INCLUDED);
        dto.setTaxes(fei.upce.nnpro.remax.realestates.entity.enums.Taxes.INCLUDED);
        dto.setAvailableFrom(java.time.ZonedDateTime.now());
        dto.setBasement(true);
        dto.setBuildingProperties(new fei.upce.nnpro.remax.realestates.entity.BuildingProperties());
        dto.setEquipment(fei.upce.nnpro.remax.realestates.entity.enums.Equipment.FURNISHED);
        dto.setUtilities(new fei.upce.nnpro.remax.realestates.entity.Utilities());
        dto.setTransportPossibilities(new fei.upce.nnpro.remax.realestates.entity.TransportPossibilities());
        dto.setCivicAmenities(new fei.upce.nnpro.remax.realestates.entity.CivicAmenities());

        when(realEstateRepository.findById(id)).thenReturn(Optional.of(existing));
        when(realEstateRepository.save(existing)).thenReturn(existing);

        // Act
        RealEstate result = realEstateService.updateRealEstate(id, dto);

        // Assert
        assertThat(result.getName()).isEqualTo("Updated Name");
        assertThat(result.getDescription()).isEqualTo("Updated Description");
        assertThat(result.getStatus()).isEqualTo(fei.upce.nnpro.remax.realestates.entity.enums.Status.BOUGHT);
        assertThat(result.getUsableArea()).isEqualTo(120.5);
        assertThat(result.getContractType()).isEqualTo(fei.upce.nnpro.remax.realestates.entity.enums.ContractType.RENTAL);
        assertThat(result.getPriceDisclosure()).isEqualTo(fei.upce.nnpro.remax.realestates.entity.enums.PriceDisclosure.ASK);
        assertThat(result.getCommission()).isEqualTo(fei.upce.nnpro.remax.realestates.entity.enums.Commission.INCLUDED);
        assertThat(result.getTaxes()).isEqualTo(fei.upce.nnpro.remax.realestates.entity.enums.Taxes.INCLUDED);
        assertThat(result.getAvailableFrom()).isNotNull();
        assertThat(result.isBasement()).isTrue();
        assertThat(result.getBuildingProperties()).isNotNull();
        assertThat(result.getEquipment()).isNotNull();
        assertThat(result.getUtilities()).isNotNull();
        assertThat(result.getTransportPossibilities()).isNotNull();
        assertThat(result.getCivicAmenities()).isNotNull();
    }

    @Test
    @DisplayName("Update: Should not update usableArea when zero or negative")
    void updateRealEstate_UsableAreaZero() {
        // Arrange
        Long id = 1L;
        Apartment existing = new Apartment();
        existing.setId(id);
        existing.setUsableArea(100.0);

        RealEstateDto dto = new RealEstateDto();
        dto.setUsableArea(0.0);

        when(realEstateRepository.findById(id)).thenReturn(Optional.of(existing));
        when(realEstateRepository.save(existing)).thenReturn(existing);

        // Act
        RealEstate result = realEstateService.updateRealEstate(id, dto);

        // Assert
        assertThat(result.getUsableArea()).isEqualTo(100.0);
    }

    @Test
    @DisplayName("Update: Should update usableArea when positive")
    void updateRealEstate_UsableAreaPositive() {
        // Arrange
        Long id = 1L;
        Apartment existing = new Apartment();
        existing.setId(id);
        existing.setUsableArea(100.0);

        RealEstateDto dto = new RealEstateDto();
        dto.setUsableArea(150.5);

        when(realEstateRepository.findById(id)).thenReturn(Optional.of(existing));
        when(realEstateRepository.save(existing)).thenReturn(existing);

        // Act
        RealEstate result = realEstateService.updateRealEstate(id, dto);

        // Assert
        assertThat(result.getUsableArea()).isEqualTo(150.5);
    }

    @Test
    @DisplayName("Update: Should handle basement field (primitive boolean)")
    void updateRealEstate_BasementField() {
        // Arrange
        Long id = 1L;
        Apartment existing = new Apartment();
        existing.setId(id);
        existing.setBasement(false);

        RealEstateDto dto = new RealEstateDto();
        dto.setBasement(true);

        when(realEstateRepository.findById(id)).thenReturn(Optional.of(existing));
        when(realEstateRepository.save(existing)).thenReturn(existing);

        // Act
        RealEstate result = realEstateService.updateRealEstate(id, dto);

        // Assert
        assertThat(result.isBasement()).isTrue();
    }

    @Test
    @DisplayName("Create: Should handle entity with both address and price")
    void createRealEstate_WithBothAddressAndPrice() {
        // Arrange
        RealEstateDto dto = new RealEstateDto();
        dto.setPrice(10000000.0);
        dto.setName("Luxury Apartment");
        Address address = new Address();
        dto.setAddress(address);

        RealEstate mappedEntity = new Apartment();
        mappedEntity.setAddress(address);
        mappedEntity.setName("Luxury Apartment");
        mappedEntity.setPriceHistory(null);

        Address savedAddress = new Address();
        when(realEstateMapper.toEntity(dto)).thenReturn(mappedEntity);
        when(addressService.save(any(Address.class))).thenReturn(savedAddress);
        when(realEstateRepository.save(any(RealEstate.class))).thenAnswer(invocation -> {
            RealEstate re = invocation.getArgument(0);
            re.setId(123L);
            return re;
        });

        // Act
        RealEstate result = realEstateService.createRealEstate(dto);

        // Assert
        verify(addressService).save(address);
        verify(realEstateRepository).save(mappedEntity);
        assertThat(result.getPriceHistory()).isNotNull().hasSize(1);
        assertThat(result.getPriceHistory().getFirst().getPrice()).isEqualTo(10000000.0);
        assertThat(result.getId()).isEqualTo(123L);
    }

    @Test
    @DisplayName("Search: Should handle empty filter")
    void searchRealEstates_EmptyFilter() {
        // Arrange
        RealEstateFilterDto emptyFilter = new RealEstateFilterDto();
        Pageable pageable = PageRequest.of(0, 20);
        Page<RealEstate> expectedPage = new PageImpl<>(new ArrayList<>(), pageable, 0);

        when(realEstateRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(expectedPage);

        // Act
        Page<RealEstate> result = realEstateService.searchRealEstates(emptyFilter, pageable);

        // Assert
        assertThat(result).isEmpty();
        verify(realEstateRepository).findAll(any(Specification.class), any(Pageable.class));
    }

    @Test
    @DisplayName("Update: Should not modify price history when price is null")
    void updateRealEstate_NullPrice() {
        // Arrange
        Long id = 1L;
        RealEstate existing = new Apartment();
        existing.setId(id);
        PriceHistory ph = new PriceHistory();
        ph.setPrice(5000.0);
        existing.setPriceHistory(new ArrayList<>(List.of(ph)));

        RealEstateDto dto = new RealEstateDto();
        dto.setPrice(null);

        when(realEstateRepository.findById(id)).thenReturn(Optional.of(existing));
        when(realEstateRepository.save(existing)).thenReturn(existing);

        // Act
        RealEstate result = realEstateService.updateRealEstate(id, dto);

        // Assert
        assertThat(result.getPriceHistory()).hasSize(1);
        assertThat(result.getPriceHistory().getFirst().getPrice()).isEqualTo(5000.0);
    }
}