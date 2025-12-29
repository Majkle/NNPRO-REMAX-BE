package fei.upce.nnpro.remax.realestates.factory;

import fei.upce.nnpro.remax.realestates.dto.RealEstateDto;
import fei.upce.nnpro.remax.realestates.entity.Apartment;
import fei.upce.nnpro.remax.realestates.entity.House;
import fei.upce.nnpro.remax.realestates.entity.Land;
import fei.upce.nnpro.remax.realestates.entity.RealEstate;
import fei.upce.nnpro.remax.realestates.entity.enums.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RealEstateFactoryTest {

    private RealEstateFactory factory;

    @BeforeEach
    void setUp() {
        factory = new RealEstateFactory();
    }

    @Test
    @DisplayName("Create Entity: Should create Apartment with all fields mapped")
    void createEntity_Apartment() {
        // Arrange
        RealEstateDto dto = createCommonDto();
        dto.setType(RealEstateType.APARTMENT);
        dto.setFloor(5);
        dto.setTotalFloors(10);
        dto.setElevator(true);
        dto.setBalcony(false);
        dto.setRooms(3);
        dto.setOwnershipType(ApartmentOwnershipType.OWNERSHIP);

        // Act
        RealEstate result = factory.createEntity(dto);

        // Assert
        assertThat(result).isInstanceOf(Apartment.class);
        assertCommonFields(result, dto);

        Apartment apt = (Apartment) result;
        assertThat(apt.getFloor()).isEqualTo(5);
        assertThat(apt.getTotalFloors()).isEqualTo(10);
        assertThat(apt.isElevator()).isTrue();
        assertThat(apt.isBalcony()).isFalse();
        assertThat(apt.getRooms()).isEqualTo(3);
        assertThat(apt.getOwnershipType()).isEqualTo(ApartmentOwnershipType.OWNERSHIP);
    }

    @Test
    @DisplayName("Create Entity: Should create House with all fields mapped")
    void createEntity_House() {
        // Arrange
        RealEstateDto dto = createCommonDto();
        dto.setType(RealEstateType.HOUSE);
        dto.setPlotArea(500.0);
        dto.setStories(2);
        dto.setHouseType(HouseType.DETACHED);

        // Act
        RealEstate result = factory.createEntity(dto);

        // Assert
        assertThat(result).isInstanceOf(House.class);
        assertCommonFields(result, dto);

        House house = (House) result;
        assertThat(house.getPlotArea()).isEqualTo(500.0);
        assertThat(house.getStories()).isEqualTo(2);
        assertThat(house.getHouseType()).isEqualTo(HouseType.DETACHED);
    }

    @Test
    @DisplayName("Create Entity: Should create Land with all fields mapped")
    void createEntity_Land() {
        // Arrange
        RealEstateDto dto = createCommonDto();
        dto.setType(RealEstateType.LAND);
        dto.setIsForHousing(true);

        // Act
        RealEstate result = factory.createEntity(dto);

        // Assert
        assertThat(result).isInstanceOf(Land.class);
        assertCommonFields(result, dto);

        Land land = (Land) result;
        assertThat(land.isForHousing()).isTrue();
    }

    @Test
    @DisplayName("Create Entity: Should throw exception if type is null")
    void createEntity_NullType() {
        RealEstateDto dto = new RealEstateDto();
        dto.setType(null);

        assertThatThrownBy(() -> factory.createEntity(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("RealEstateType cannot be null");
    }

    @Test
    @DisplayName("Create Entity: Should handle null/default values for specific fields")
    void createEntity_NullSpecifics() {
        // Check that factory handles nulls gracefully (e.g. converting null Integer to 0 or false)
        RealEstateDto dto = createCommonDto();
        dto.setType(RealEstateType.APARTMENT);
        dto.setElevator(null); // Should become false
        dto.setRooms(null);    // Should become 0

        RealEstate result = factory.createEntity(dto);

        Apartment apt = (Apartment) result;
        assertThat(apt.isElevator()).isFalse();
        assertThat(apt.getRooms()).isZero();
    }

    // --- Helpers ---

    private RealEstateDto createCommonDto() {
        RealEstateDto dto = new RealEstateDto();
        dto.setName("Test Property");
        dto.setDescription("Description");
        dto.setStatus(Status.AVAILABLE);
        dto.setUsableArea(100.0);
        dto.setContractType(ContractType.SALE);
        dto.setPriceDisclosure(PriceDisclosure.ASK);
        dto.setCommission(Commission.INCLUDED);
        dto.setTaxes(Taxes.INCLUDED);
        dto.setAvailableFrom(ZonedDateTime.now());
        dto.setBasement(true);
        dto.setEquipment(Equipment.FURNISHED);
        return dto;
    }

    private void assertCommonFields(RealEstate entity, RealEstateDto dto) {
        assertThat(entity.getName()).isEqualTo(dto.getName());
        assertThat(entity.getDescription()).isEqualTo(dto.getDescription());
        assertThat(entity.getStatus()).isEqualTo(dto.getStatus());
        assertThat(entity.getUsableArea()).isEqualTo(dto.getUsableArea());
        assertThat(entity.getContractType()).isEqualTo(dto.getContractType());
        assertThat(entity.getPriceDisclosure()).isEqualTo(dto.getPriceDisclosure());
        assertThat(entity.getCommission()).isEqualTo(dto.getCommission());
        assertThat(entity.getTaxes()).isEqualTo(dto.getTaxes());
        assertThat(entity.getAvailableFrom()).isEqualTo(dto.getAvailableFrom());
        assertThat(entity.isBasement()).isEqualTo(dto.isBasement());
        assertThat(entity.getEquipment()).isEqualTo(dto.getEquipment());
    }
}