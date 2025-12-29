package fei.upce.nnpro.remax.realestates.factory.strategy;

import fei.upce.nnpro.remax.realestates.dto.RealEstateDto;
import fei.upce.nnpro.remax.realestates.entity.House;
import fei.upce.nnpro.remax.realestates.entity.Land;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LandUpdateStrategyTest {

    private LandUpdateStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new LandUpdateStrategy();
    }

    @Test
    @DisplayName("Supports: Should return true only for Land")
    void supports() {
        assertThat(strategy.supports(new Land())).isTrue();
        assertThat(strategy.supports(new House())).isFalse();
    }

    @Test
    @DisplayName("Update: Should update fields present in DTO")
    void update_UpdatesFields() {
        Land land = new Land();
        land.setForHousing(false);

        RealEstateDto dto = new RealEstateDto();
        dto.setIsForHousing(true);

        strategy.update(land, dto);

        assertThat(land.isForHousing()).isTrue();
    }

    @Test
    @DisplayName("Update: Should ignore null fields in DTO")
    void update_IgnoreNulls() {
        Land land = new Land();
        land.setForHousing(true);

        RealEstateDto dto = new RealEstateDto();
        dto.setIsForHousing(null);

        strategy.update(land, dto);

        assertThat(land.isForHousing()).isTrue(); // Unchanged
    }
}
