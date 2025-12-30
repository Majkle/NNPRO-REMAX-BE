package fei.upce.nnpro.remax.realestates.dto;

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
        assertThat(dto.getBasement()).isTrue();
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
}
