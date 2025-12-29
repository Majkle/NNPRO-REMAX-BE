package fei.upce.nnpro.remax.realestates.factory.strategy;

import fei.upce.nnpro.remax.realestates.dto.RealEstateDto;
import fei.upce.nnpro.remax.realestates.entity.Land;
import fei.upce.nnpro.remax.realestates.entity.RealEstate;
import org.springframework.stereotype.Component;

@Component
public class LandUpdateStrategy implements RealEstateUpdateStrategy {

    @Override
    public boolean supports(RealEstate realEstate) {
        return realEstate instanceof Land;
    }

    @Override
    public void update(RealEstate realEstate, RealEstateDto dto) {
        Land land = (Land) realEstate;

        if (dto.getIsForHousing() != null) {
            land.setForHousing(dto.getIsForHousing());
        }
    }
}
