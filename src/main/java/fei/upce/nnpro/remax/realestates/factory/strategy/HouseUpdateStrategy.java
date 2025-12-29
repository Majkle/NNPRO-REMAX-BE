package fei.upce.nnpro.remax.realestates.factory.strategy;

import fei.upce.nnpro.remax.realestates.dto.RealEstateDto;
import fei.upce.nnpro.remax.realestates.entity.House;
import fei.upce.nnpro.remax.realestates.entity.RealEstate;
import org.springframework.stereotype.Component;

@Component
public class HouseUpdateStrategy implements RealEstateUpdateStrategy {

    @Override
    public boolean supports(RealEstate realEstate) {
        return realEstate instanceof House;
    }

    @Override
    public void update(RealEstate realEstate, RealEstateDto dto) {
        House house = (House) realEstate;

        if (dto.getPlotArea() != null) {
            house.setPlotArea(dto.getPlotArea());
        }
        if (dto.getHouseType() != null) {
            house.setHouseType(dto.getHouseType());
        }
        if (dto.getStories() != null) {
            house.setStories(dto.getStories());
        }
    }
}
