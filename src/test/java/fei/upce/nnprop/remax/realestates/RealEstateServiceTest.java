package fei.upce.nnprop.remax.realestates;

import fei.upce.nnprop.remax.address.AddressService;
import fei.upce.nnprop.remax.model.Image;
import fei.upce.nnprop.remax.model.ImageRepository;
import fei.upce.nnprop.remax.realestates.RealEstateMapper;
import fei.upce.nnprop.remax.realestates.dto.RealEstateDto;
import fei.upce.nnprop.remax.realestates.dto.RealEstateFilterDto;
import fei.upce.nnprop.remax.model.realestates.entity.Address;
import fei.upce.nnprop.remax.model.realestates.entity.Apartment;
import fei.upce.nnprop.remax.model.realestates.entity.PriceHistory;
import fei.upce.nnprop.remax.model.realestates.entity.RealEstate;
import fei.upce.nnprop.remax.realestates.repository.RealEstateRepository;
import fei.upce.nnprop.remax.realestates.service.RealEstateService;
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
}