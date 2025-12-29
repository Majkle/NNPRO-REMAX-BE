package fei.upce.nnpro.remax.realestates.service;

import fei.upce.nnpro.remax.realestates.entity.Apartment;
import fei.upce.nnpro.remax.realestates.entity.PriceHistory;
import fei.upce.nnpro.remax.realestates.entity.RealEstate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RealEstatePriceHelperTest {

    private RealEstatePriceHelper helper;

    @BeforeEach
    void setUp() {
        helper = new RealEstatePriceHelper();
    }

    @Test
    @DisplayName("Initialize: Should add price history when price is provided")
    void initializePrice_WithPrice() {
        RealEstate estate = new Apartment();
        Double price = 5000.0;

        helper.initializePrice(estate, price);

        assertThat(estate.getPriceHistory()).hasSize(1);
        PriceHistory history = estate.getPriceHistory().getFirst();
        assertThat(history.getPrice()).isEqualTo(5000.0);
        assertThat(history.getRealEstate()).isEqualTo(estate);
        assertThat(history.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Initialize: Should do nothing when price is null")
    void initializePrice_NullPrice() {
        RealEstate estate = new Apartment();

        helper.initializePrice(estate, null);

        assertThat(estate.getPriceHistory()).isNullOrEmpty();
    }

    @Test
    @DisplayName("Update: Should add new history entry when price changes")
    void updatePrice_Changed() {
        RealEstate estate = new Apartment();
        PriceHistory old = new PriceHistory(1000.0, estate);
        // Mutable list required
        estate.setPriceHistory(new ArrayList<>(List.of(old)));

        helper.updatePrice(estate, 1500.0);

        assertThat(estate.getPriceHistory()).hasSize(2);
        assertThat(estate.getPriceHistory().getLast().getPrice()).isEqualTo(1500.0);
    }

    @Test
    @DisplayName("Update: Should do nothing when price is same")
    void updatePrice_Same() {
        RealEstate estate = new Apartment();
        PriceHistory old = new PriceHistory(1000.0, estate);
        estate.setPriceHistory(new ArrayList<>(List.of(old)));

        helper.updatePrice(estate, 1000.0); // Same price

        assertThat(estate.getPriceHistory()).hasSize(1);
    }

    @Test
    @DisplayName("Update: Should do nothing when new price is null")
    void updatePrice_NullNewPrice() {
        RealEstate estate = new Apartment();
        PriceHistory old = new PriceHistory(1000.0, estate);
        estate.setPriceHistory(new ArrayList<>(List.of(old)));

        helper.updatePrice(estate, null);

        assertThat(estate.getPriceHistory()).hasSize(1);
    }

    @Test
    @DisplayName("Update: Should initialize list if history is null but price provided")
    void updatePrice_NullHistory() {
        RealEstate estate = new Apartment();
        estate.setPriceHistory(null);

        helper.updatePrice(estate, 2000.0);

        assertThat(estate.getPriceHistory()).hasSize(1);
        assertThat(estate.getPriceHistory().getFirst().getPrice()).isEqualTo(2000.0);
    }
}
