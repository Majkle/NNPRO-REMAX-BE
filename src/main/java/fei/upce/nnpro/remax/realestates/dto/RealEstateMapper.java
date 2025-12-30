package fei.upce.nnpro.remax.realestates.dto;

import fei.upce.nnpro.remax.address.dto.AddressMapper;
import fei.upce.nnpro.remax.images.entity.Image;
import fei.upce.nnpro.remax.realestates.entity.*;
import fei.upce.nnpro.remax.realestates.entity.enums.RealEstateType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RealEstateMapper {

    private final AddressMapper addressMapper;

    /**
     * Converts a RealEstate Entity to a DTO.
     * (Useful for the Controller layer, as the Service returns Entities)
     */
    public RealEstateDto toDto(RealEstate entity) {
        if (entity == null) return null;

        RealEstateDto dto = new RealEstateDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setStatus(entity.getStatus());
        dto.setUsableArea(entity.getUsableArea());
        dto.setContractType(entity.getContractType());
        dto.setPriceDisclosure(entity.getPriceDisclosure());
        dto.setCommission(entity.getCommission());
        dto.setTaxes(entity.getTaxes());
        dto.setAvailableFrom(entity.getAvailableFrom());
        dto.setBasement(entity.isBasement());

        dto.setAddress(addressMapper.toDto(entity.getAddress()));
        dto.setBuildingProperties(entity.getBuildingProperties());
        dto.setEquipment(entity.getEquipment());
        dto.setUtilities(entity.getUtilities());
        dto.setTransportPossibilities(entity.getTransportPossibilities());
        dto.setCivicAmenities(entity.getCivicAmenities());

        if (entity.getRealtor() != null) {
            dto.setRealtorId(entity.getRealtor().getId());
        }

        List<PriceHistory> history = entity.getPriceHistory();
        if (history != null && !history.isEmpty()) {
            dto.setPrice(history.getLast().getPrice());
        }

        if (entity.getImages() != null && !entity.getImages().isEmpty()) {
            dto.setImages(entity.getImages().stream()
                    .map(Image::getId)
                    .collect(Collectors.toList()));
        }

        if (entity instanceof Apartment apt) {
            dto.setType(RealEstateType.APARTMENT);
            dto.setOwnershipType(apt.getOwnershipType());
            dto.setFloor(apt.getFloor());
            dto.setTotalFloors(apt.getTotalFloors());
            dto.setElevator(apt.isElevator());
            dto.setBalcony(apt.isBalcony());
            dto.setRooms(apt.getRooms());
        } else if (entity instanceof House house) {
            dto.setType(RealEstateType.HOUSE);
            dto.setPlotArea(house.getPlotArea());
            dto.setHouseType(house.getHouseType());
            dto.setStories(house.getStories());
        } else if (entity instanceof Land land) {
            dto.setType(RealEstateType.LAND);
            dto.setIsForHousing(land.isForHousing());
        }

        return dto;
    }

}