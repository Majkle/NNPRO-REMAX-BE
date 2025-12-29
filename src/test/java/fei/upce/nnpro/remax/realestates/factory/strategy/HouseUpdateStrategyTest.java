package fei.upce.nnpro.remax.realestates.factory.strategy;

import fei.upce.nnpro.remax.realestates.dto.RealEstateDto;
import fei.upce.nnpro.remax.realestates.entity.Apartment;
import fei.upce.nnpro.remax.realestates.entity.House;
import fei.upce.nnpro.remax.realestates.entity.enums.HouseType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HouseUpdateStrategyTest {

    private HouseUpdateStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new HouseUpdateStrategy();
    }

    @Test
    @DisplayName("Supports: Should return true only for House")
    void supports() {
        assertThat(strategy.supports(new House())).isTrue();
        assertThat(strategy.supports(new Apartment())).isFalse();
    }

    @Test
    @DisplayName("Update: Should update fields present in DTO")
    void update_UpdatesFields() {
        House house = new House();
        house.setPlotArea(100.0);

        RealEstateDto dto = new RealEstateDto();
        dto.setPlotArea(500.0);
        dto.setStories(3);
        dto.setHouseType(HouseType.SEMI_DETACHED);

        strategy.update(house, dto);

        assertThat(house.getPlotArea()).isEqualTo(500.0);
        assertThat(house.getStories()).isEqualTo(3);
        assertThat(house.getHouseType()).isEqualTo(HouseType.SEMI_DETACHED);
    }

    @Test
    @DisplayName("Update: Should ignore null fields in DTO")
    void update_IgnoreNulls() {
        House house = new House();
        house.setPlotArea(200.0);
        house.setStories(1);

        RealEstateDto dto = new RealEstateDto();
        dto.setPlotArea(null);
        dto.setStories(null);

        strategy.update(house, dto);

        assertThat(house.getPlotArea()).isEqualTo(200.0); // Unchanged
        assertThat(house.getStories()).isEqualTo(1); // Unchanged
    }
}
