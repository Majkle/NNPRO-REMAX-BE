package fei.upce.nnpro.remax.realestates.service;

import fei.upce.nnpro.remax.realestates.entity.PriceHistory;
import fei.upce.nnpro.remax.realestates.entity.RealEstate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RealEstatePriceHelper {

    private static final Logger log = LoggerFactory.getLogger(RealEstatePriceHelper.class);

    public void initializePrice(RealEstate realEstate, Double price) {
        if (price == null) return;

        PriceHistory history = new PriceHistory(price, realEstate);

        realEstate.setPriceHistory(new ArrayList<>(List.of(history)));
        log.debug("Initialized price history for estate id={} with price={}", realEstate.getId(), price);
    }

    public void updatePrice(RealEstate realEstate, Double newPrice) {
        if (newPrice == null) return;

        double currentPrice = getCurrentPrice(realEstate);

        if (!newPrice.equals(currentPrice)) {
            PriceHistory history = new PriceHistory(newPrice, realEstate);

            if (realEstate.getPriceHistory() == null) {
                realEstate.setPriceHistory(new ArrayList<>());
            }
            realEstate.getPriceHistory().add(history);

            log.info("Price changed for estate id={} from={} to={}", realEstate.getId(), currentPrice, newPrice);
        }
    }

    private double getCurrentPrice(RealEstate realEstate) {
        if (realEstate.getPriceHistory() != null && !realEstate.getPriceHistory().isEmpty()) {
            return realEstate.getPriceHistory().getLast().getPrice();
        }
        return 0.0;
    }
}
