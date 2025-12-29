package fei.upce.nnpro.remax.realestates.factory.strategy;

import fei.upce.nnpro.remax.realestates.dto.RealEstateDto;
import fei.upce.nnpro.remax.realestates.entity.Apartment;
import fei.upce.nnpro.remax.realestates.entity.House;
import fei.upce.nnpro.remax.realestates.entity.enums.ApartmentOwnershipType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApartmentUpdateStrategyTest {

    private ApartmentUpdateStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new ApartmentUpdateStrategy();
    }

    @Test
    @DisplayName("Supports: Should return true only for Apartment")
    void supports() {
        assertThat(strategy.supports(new Apartment())).isTrue();
        assertThat(strategy.supports(new House())).isFalse();
    }

    @Test
    @DisplayName("Update: Should update fields present in DTO")
    void update_UpdatesFields() {
        Apartment apt = new Apartment();
        apt.setFloor(1);
        apt.setElevator(false);

        RealEstateDto dto = new RealEstateDto();
        dto.setFloor(5);
        dto.setTotalFloors(10);
        dto.setElevator(true);
        dto.setBalcony(true);
        dto.setRooms(4);
        dto.setOwnershipType(ApartmentOwnershipType.COOPERATIVE_OWNERSHIP);

        strategy.update(apt, dto);

        assertThat(apt.getFloor()).isEqualTo(5);
        assertThat(apt.getTotalFloors()).isEqualTo(10);
        assertThat(apt.isElevator()).isTrue();
        assertThat(apt.isBalcony()).isTrue();
        assertThat(apt.getRooms()).isEqualTo(4);
        assertThat(apt.getOwnershipType()).isEqualTo(ApartmentOwnershipType.COOPERATIVE_OWNERSHIP);
    }

    @Test
    @DisplayName("Update: Should ignore null fields in DTO")
    void update_IgnoreNulls() {
        Apartment apt = new Apartment();
        apt.setFloor(2);
        apt.setTotalFloors(5);

        RealEstateDto dto = new RealEstateDto();
        dto.setFloor(null);
        dto.setTotalFloors(null);

        strategy.update(apt, dto);

        assertThat(apt.getFloor()).isEqualTo(2); // Unchanged
        assertThat(apt.getTotalFloors()).isEqualTo(5); // Unchanged
    }
}
