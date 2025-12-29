package fei.upce.nnpro.remax.realestates.factory.strategy;

import fei.upce.nnpro.remax.realestates.dto.RealEstateDto;
import fei.upce.nnpro.remax.realestates.entity.Apartment;
import fei.upce.nnpro.remax.realestates.entity.RealEstate;
import org.springframework.stereotype.Component;

@Component
public class ApartmentUpdateStrategy implements RealEstateUpdateStrategy {

    @Override
    public boolean supports(RealEstate realEstate) {
        return realEstate instanceof Apartment;
    }

    @Override
    public void update(RealEstate realEstate, RealEstateDto dto) {
        Apartment apt = (Apartment) realEstate;

        if (dto.getOwnershipType() != null) {
            apt.setOwnershipType(dto.getOwnershipType());
        }
        if (dto.getFloor() != null) {
            apt.setFloor(dto.getFloor());
        }
        if (dto.getTotalFloors() != null) {
            apt.setTotalFloors(dto.getTotalFloors());
        }
        if (dto.getElevator() != null) {
            apt.setElevator(dto.getElevator());
        }
        if (dto.getBalcony() != null) {
            apt.setBalcony(dto.getBalcony());
        }
        if (dto.getRooms() != null) {
            apt.setRooms(dto.getRooms());
        }
    }
}