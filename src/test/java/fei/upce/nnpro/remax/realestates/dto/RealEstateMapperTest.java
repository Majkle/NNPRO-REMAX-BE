package fei.upce.nnpro.remax.realestates.dto;

import fei.upce.nnpro.remax.address.dto.AddressDto;
import fei.upce.nnpro.remax.address.dto.AddressMapper;
import fei.upce.nnpro.remax.address.entity.Address;
import fei.upce.nnpro.remax.images.entity.Image;
import fei.upce.nnpro.remax.images.repository.ImageRepository;
import fei.upce.nnpro.remax.realestates.entity.*;
import fei.upce.nnpro.remax.realestates.entity.enums.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RealEstateMapperTest {

    @Mock
    private ImageRepository imageRepository;

    @InjectMocks
    private RealEstateMapper mapper;

    @Mock
    private AddressMapper addressMapper;

    @Test
    void toDto_WithNullEntity_ShouldReturnNull() {
        RealEstateDto dto = mapper.toDto(null);
        assertThat(dto).isNull();
    }

    @Test
    void toDto_WithApartment_ShouldMapAllFields() {
        Apartment apartment = new Apartment();
        apartment.setId(1L);
        apartment.setName("Modern Apartment");
        apartment.setDescription("Beautiful apartment in city center");
        apartment.setStatus(Status.AVAILABLE);
        apartment.setUsableArea(75.5);
        apartment.setContractType(ContractType.SALE);
        apartment.setPriceDisclosure(PriceDisclosure.ASK);
        apartment.setCommission(Commission.INCLUDED);
        apartment.setTaxes(Taxes.INCLUDED);
        apartment.setAvailableFrom(ZonedDateTime.now());
        apartment.setBasement(true);

        apartment.setOwnershipType(ApartmentOwnershipType.OWNERSHIP);
        apartment.setFloor(3);
        apartment.setTotalFloors(5);
        apartment.setElevator(true);
        apartment.setBalcony(true);
        apartment.setRooms(3);

        Address address = new Address();
        apartment.setAddress(address);

        PriceHistory priceHistory = new PriceHistory();
        priceHistory.setPrice(250000.0);
        apartment.setPriceHistory(List.of(priceHistory));

        Image image1 = new Image();
        image1.setId(10L);
        Image image2 = new Image();
        image2.setId(20L);
        apartment.setImages(Arrays.asList(image1, image2));

        RealEstateDto dto = mapper.toDto(apartment);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("Modern Apartment");
        assertThat(dto.getDescription()).isEqualTo("Beautiful apartment in city center");
        assertThat(dto.getStatus()).isEqualTo(Status.AVAILABLE);
        assertThat(dto.getUsableArea()).isEqualTo(75.5);
        assertThat(dto.getContractType()).isEqualTo(ContractType.SALE);
        assertThat(dto.getPriceDisclosure()).isEqualTo(PriceDisclosure.ASK);
        assertThat(dto.getCommission()).isEqualTo(Commission.INCLUDED);
        assertThat(dto.getTaxes()).isEqualTo(Taxes.INCLUDED);
        assertThat(dto.isBasement()).isTrue();
        assertThat(dto.getPrice()).isEqualTo(250000.0);
        assertThat(dto.getType()).isEqualTo(RealEstateType.APARTMENT);
        assertThat(dto.getOwnershipType()).isEqualTo(ApartmentOwnershipType.OWNERSHIP);
        assertThat(dto.getFloor()).isEqualTo(3);
        assertThat(dto.getTotalFloors()).isEqualTo(5);
        assertThat(dto.getElevator()).isTrue();
        assertThat(dto.getBalcony()).isTrue();
        assertThat(dto.getRooms()).isEqualTo(3);
        assertThat(dto.getImages()).containsExactly(10L, 20L);
    }

    @Test
    void toDto_WithHouse_ShouldMapHouseSpecificFields() {
        House house = new House();
        house.setId(2L);
        house.setName("Family House");
        house.setDescription("Spacious house with garden");
        house.setStatus(Status.RESERVED);
        house.setUsableArea(150.0);
        house.setContractType(ContractType.RENTAL);
        house.setPriceDisclosure(PriceDisclosure.ASK);
        house.setCommission(Commission.EXCLUDED);
        house.setTaxes(Taxes.EXCLUDED);
        house.setBasement(false);

        house.setPlotArea(500.0);
        house.setHouseType(HouseType.DETACHED);
        house.setStories(2);

        Address address = new Address();
        house.setAddress(address);

        PriceHistory priceHistory = new PriceHistory();
        priceHistory.setPrice(2000.0);
        house.setPriceHistory(List.of(priceHistory));

        RealEstateDto dto = mapper.toDto(house);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getType()).isEqualTo(RealEstateType.HOUSE);
        assertThat(dto.getPlotArea()).isEqualTo(500.0);
        assertThat(dto.getHouseType()).isEqualTo(HouseType.DETACHED);
        assertThat(dto.getStories()).isEqualTo(2);
        assertThat(dto.getPrice()).isEqualTo(2000.0);
    }

    @Test
    void toDto_WithLand_ShouldMapLandSpecificFields() {
        Land land = new Land();
        land.setId(3L);
        land.setName("Building Plot");
        land.setDescription("Great location for new construction");
        land.setStatus(Status.BOUGHT);
        land.setUsableArea(1000.0);
        land.setContractType(ContractType.SALE);
        land.setPriceDisclosure(PriceDisclosure.NOT_DISCLOSED);
        land.setCommission(Commission.INCLUDED);
        land.setTaxes(Taxes.INCLUDED);
        land.setBasement(false);

        land.setForHousing(true);

        Address address = new Address();
        land.setAddress(address);

        RealEstateDto dto = mapper.toDto(land);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(3L);
        assertThat(dto.getType()).isEqualTo(RealEstateType.LAND);
        assertThat(dto.getIsForHousing()).isTrue();
    }

    @Test
    void toDto_WithNoPriceHistory_ShouldNotSetPrice() {
        Apartment apartment = new Apartment();
        apartment.setId(1L);
        apartment.setName("Apartment");
        apartment.setDescription("Description");
        apartment.setStatus(Status.AVAILABLE);
        apartment.setUsableArea(50.0);
        apartment.setContractType(ContractType.SALE);
        apartment.setPriceDisclosure(PriceDisclosure.ASK);
        apartment.setCommission(Commission.INCLUDED);
        apartment.setTaxes(Taxes.INCLUDED);

        Address address = new Address();
        apartment.setAddress(address);

        RealEstateDto dto = mapper.toDto(apartment);

        assertThat(dto).isNotNull();
        assertThat(dto.getPrice()).isNull();
    }

    @Test
    void toDto_WithEmptyPriceHistory_ShouldNotSetPrice() {
        Apartment apartment = new Apartment();
        apartment.setId(1L);
        apartment.setName("Apartment");
        apartment.setDescription("Description");
        apartment.setStatus(Status.AVAILABLE);
        apartment.setUsableArea(50.0);
        apartment.setContractType(ContractType.SALE);
        apartment.setPriceDisclosure(PriceDisclosure.ASK);
        apartment.setCommission(Commission.INCLUDED);
        apartment.setTaxes(Taxes.INCLUDED);

        Address address = new Address();
        apartment.setAddress(address);

        apartment.setPriceHistory(Collections.emptyList());

        RealEstateDto dto = mapper.toDto(apartment);

        assertThat(dto).isNotNull();
        assertThat(dto.getPrice()).isNull();
    }

    @Test
    void toDto_WithNoImages_ShouldNotSetImageIds() {
        Apartment apartment = new Apartment();
        apartment.setId(1L);
        apartment.setName("Apartment");
        apartment.setDescription("Description");
        apartment.setStatus(Status.AVAILABLE);
        apartment.setUsableArea(50.0);
        apartment.setContractType(ContractType.SALE);
        apartment.setPriceDisclosure(PriceDisclosure.ASK);
        apartment.setCommission(Commission.INCLUDED);
        apartment.setTaxes(Taxes.INCLUDED);

        Address address = new Address();
        apartment.setAddress(address);

        RealEstateDto dto = mapper.toDto(apartment);

        assertThat(dto).isNotNull();
        assertThat(dto.getImages()).isEmpty();
    }

    @Test
    void toEntity_WithApartmentDto_ShouldCreateApartment() {
        RealEstateDto dto = new RealEstateDto();
        dto.setType(RealEstateType.APARTMENT);
        dto.setName("Test Apartment");
        dto.setDescription("Test Description");
        dto.setStatus(Status.AVAILABLE);
        dto.setUsableArea(60.0);
        dto.setContractType(ContractType.SALE);
        dto.setPriceDisclosure(PriceDisclosure.ASK);
        dto.setCommission(Commission.INCLUDED);
        dto.setTaxes(Taxes.INCLUDED);
        dto.setBasement(false);

        // Change: Use AddressDto instead of Address
        AddressDto addressDto = new AddressDto();
        dto.setAddress(addressDto);

        // Change: Mock the AddressMapper behavior
        Address addressEntity = new Address();
        when(addressMapper.toEntity(addressDto)).thenReturn(addressEntity);

        RealEstate entity = mapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity).isInstanceOf(Apartment.class);
        assertThat(entity.getName()).isEqualTo("Test Apartment");
        assertThat(entity.getDescription()).isEqualTo("Test Description");
        assertThat(entity.getStatus()).isEqualTo(Status.AVAILABLE);
        assertThat(entity.getUsableArea()).isEqualTo(60.0);
        assertThat(entity.getContractType()).isEqualTo(ContractType.SALE);
        assertThat(entity.getPriceDisclosure()).isEqualTo(PriceDisclosure.ASK);
        assertThat(entity.getCommission()).isEqualTo(Commission.INCLUDED);
        assertThat(entity.getTaxes()).isEqualTo(Taxes.INCLUDED);
        assertThat(entity.isBasement()).isFalse();
        assertThat(entity.getAddress()).isEqualTo(addressEntity); // Verify address mapping
    }

    @Test
    void toEntity_WithHouseDto_ShouldCreateHouse() {
        RealEstateDto dto = new RealEstateDto();
        dto.setType(RealEstateType.HOUSE);
        dto.setName("Test House");
        dto.setDescription("Test Description");
        dto.setUsableArea(120.0);
        dto.setContractType(ContractType.RENTAL);
        dto.setPriceDisclosure(PriceDisclosure.ASK);
        dto.setCommission(Commission.EXCLUDED);
        dto.setTaxes(Taxes.EXCLUDED);
        dto.setBasement(true);

        // Change: Use AddressDto
        AddressDto addressDto = new AddressDto();
        dto.setAddress(addressDto);

        // Change: Mock AddressMapper
        when(addressMapper.toEntity(addressDto)).thenReturn(new Address());

        RealEstate entity = mapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity).isInstanceOf(House.class);
        assertThat(entity.getName()).isEqualTo("Test House");
        assertThat(entity.getStatus()).isEqualTo(Status.AVAILABLE);
    }

    @Test
    void toEntity_WithLandDto_ShouldCreateLand() {
        RealEstateDto dto = new RealEstateDto();
        dto.setType(RealEstateType.LAND);
        dto.setName("Test Land");
        dto.setDescription("Test Description");
        dto.setUsableArea(800.0);
        dto.setContractType(ContractType.SALE);
        dto.setPriceDisclosure(PriceDisclosure.NOT_DISCLOSED);
        dto.setCommission(Commission.INCLUDED);
        dto.setTaxes(Taxes.INCLUDED);
        dto.setBasement(false);

        // Change: Use AddressDto
        AddressDto addressDto = new AddressDto();
        dto.setAddress(addressDto);

        // Change: Mock AddressMapper
        when(addressMapper.toEntity(addressDto)).thenReturn(new Address());

        RealEstate entity = mapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity).isInstanceOf(Land.class);
        assertThat(entity.getName()).isEqualTo("Test Land");
    }

    @Test
    void toEntity_WithNullStatus_ShouldSetDefaultStatus() {
        RealEstateDto dto = new RealEstateDto();
        dto.setType(RealEstateType.APARTMENT);
        dto.setName("Test");
        dto.setDescription("Description");
        dto.setUsableArea(50.0);
        dto.setContractType(ContractType.SALE);
        dto.setPriceDisclosure(PriceDisclosure.ASK);
        dto.setCommission(Commission.INCLUDED);
        dto.setTaxes(Taxes.INCLUDED);

        // Change: Use AddressDto
        AddressDto addressDto = new AddressDto();
        dto.setAddress(addressDto);

        // Change: Mock AddressMapper
        when(addressMapper.toEntity(addressDto)).thenReturn(new Address());

        RealEstate entity = mapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getStatus()).isEqualTo(Status.AVAILABLE);
    }

    @Test
    void toEntity_WithImageIds_ShouldFetchAndSetImages() {
        RealEstateDto dto = new RealEstateDto();
        dto.setType(RealEstateType.APARTMENT);
        dto.setName("Test");
        dto.setDescription("Description");
        dto.setUsableArea(50.0);
        dto.setContractType(ContractType.SALE);
        dto.setPriceDisclosure(PriceDisclosure.ASK);
        dto.setCommission(Commission.INCLUDED);
        dto.setTaxes(Taxes.INCLUDED);

        // Change: Use AddressDto
        AddressDto addressDto = new AddressDto();
        dto.setAddress(addressDto);

        // Change: Mock AddressMapper
        when(addressMapper.toEntity(addressDto)).thenReturn(new Address());

        dto.setImages(Arrays.asList(1L, 2L, 3L));

        Image img1 = new Image();
        img1.setId(1L);
        Image img2 = new Image();
        img2.setId(2L);
        Image img3 = new Image();
        img3.setId(3L);

        when(imageRepository.findAllById(anyList())).thenReturn(Arrays.asList(img1, img2, img3));

        RealEstate entity = mapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getImages()).hasSize(3);
        assertThat(entity.getImages()).extracting(Image::getId).containsExactly(1L, 2L, 3L);
    }

    @Test
    void toEntity_WithEmptyImageIds_ShouldSetEmptyImageList() {
        RealEstateDto dto = new RealEstateDto();
        dto.setType(RealEstateType.APARTMENT);
        dto.setName("Test");
        dto.setDescription("Description");
        dto.setUsableArea(50.0);
        dto.setContractType(ContractType.SALE);
        dto.setPriceDisclosure(PriceDisclosure.ASK);
        dto.setCommission(Commission.INCLUDED);
        dto.setTaxes(Taxes.INCLUDED);

        // Change: Use AddressDto
        AddressDto addressDto = new AddressDto();
        dto.setAddress(addressDto);

        // Change: Mock AddressMapper
        when(addressMapper.toEntity(addressDto)).thenReturn(new Address());

        dto.setImages(Collections.emptyList());

        RealEstate entity = mapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getImages()).isEmpty();
    }

    @Test
    void toEntity_WithNullImageIds_ShouldNotSetImages() {
        RealEstateDto dto = new RealEstateDto();
        dto.setType(RealEstateType.APARTMENT);
        dto.setName("Test");
        dto.setDescription("Description");
        dto.setUsableArea(50.0);
        dto.setContractType(ContractType.SALE);
        dto.setPriceDisclosure(PriceDisclosure.ASK);
        dto.setCommission(Commission.INCLUDED);
        dto.setTaxes(Taxes.INCLUDED);

        // Change: Use AddressDto
        AddressDto addressDto = new AddressDto();
        dto.setAddress(addressDto);

        // Change: Mock AddressMapper
        when(addressMapper.toEntity(addressDto)).thenReturn(new Address());

        RealEstate entity = mapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getImages()).isEmpty();
    }

    @Test
    void toDto_WithApartment_ShouldMapAddressUsingMapper() {
        Apartment apartment = new Apartment();
        apartment.setId(1L);
        Address address = new Address();
        apartment.setAddress(address);

        AddressDto addrDto = new AddressDto();
        when(addressMapper.toDto(address)).thenReturn(addrDto);

        RealEstateDto dto = mapper.toDto(apartment);

        assertThat(dto).isNotNull();
        assertThat(dto.getAddress()).isEqualTo(addrDto);
    }

    @Test
    void toEntity_WithApartmentDto_ShouldMapAddressUsingMapper() {
        RealEstateDto dto = new RealEstateDto();
        dto.setType(RealEstateType.APARTMENT);
        AddressDto addrDto = new AddressDto();
        dto.setAddress(addrDto);

        Address addressEntity = new Address();
        when(addressMapper.toEntity(addrDto)).thenReturn(addressEntity);

        RealEstate entity = mapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getAddress()).isEqualTo(addressEntity);
    }
}
