package fei.upce.nnpro.remax.realestates.factory.strategy;

import fei.upce.nnpro.remax.realestates.dto.RealEstateDto;
import fei.upce.nnpro.remax.realestates.entity.RealEstate;

public interface RealEstateUpdateStrategy {
    boolean supports(RealEstate realEstate);
    void update(RealEstate realEstate, RealEstateDto dto);
}
