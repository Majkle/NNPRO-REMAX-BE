package fei.upce.nnprop.remax.realestates;

import fei.upce.nnprop.remax.model.image.Image;
import fei.upce.nnprop.remax.images.repository.ImageRepository;
import fei.upce.nnprop.remax.realestates.dto.RealEstateDto;
import fei.upce.nnprop.remax.model.realestates.entity.*;
import fei.upce.nnprop.remax.model.realestates.enums.RealEstateType;
import fei.upce.nnprop.remax.model.realestates.enums.Status;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RealEstateMapper {

    private final ImageRepository imageRepository;

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

        dto.setAddress(entity.getAddress());
        dto.setBuildingProperties(entity.getBuildingProperties());
        dto.setEquipment(entity.getEquipment());
        dto.setUtilities(entity.getUtilities());
        dto.setTransportPossibilities(entity.getTransportPossibilities());
        dto.setCivicAmenities(entity.getCivicAmenities());

        List<PriceHistory> history = entity.getPriceHistory();
        if (history != null && !history.isEmpty()) {
            dto.setPrice(history.getLast().getPrice());
        }

        if (entity.getImage() != null && !entity.getImage().isEmpty()) {
            dto.setImageIds(entity.getImage().stream()
                    .map(Image::getId)
                    .collect(Collectors.toList()));
        }

        if (entity instanceof Apartment apt) {
            dto.setRealEstateType(RealEstateType.APARTMENT);
            dto.setOwnershipType(apt.getOwnershipType());
            dto.setFloor(apt.getFloor());
            dto.setTotalFloors(apt.getTotalFloors());
            dto.setElevator(apt.isElevator());
            dto.setBalcony(apt.isBalcony());
            dto.setRooms(apt.getRooms());
        } else if (entity instanceof House house) {
            dto.setRealEstateType(RealEstateType.HOUSE);
            dto.setPlotArea(house.getPlotArea());
            dto.setHouseType(house.getHouseType());
            dto.setStories(house.getStories());
        } else if (entity instanceof Land land) {
            dto.setRealEstateType(RealEstateType.LAND);
            dto.setIsForHousing(land.isForHousing());
        }

        return dto;
    }

    /**
     * Maps DTO data to an Entity.
     * If 'existingEntity' is null, creates a new instance based on type.
     * If 'existingEntity' is provided, updates it.
     */
    public RealEstate toEntity(RealEstateDto dto) {
        RealEstate entity = switch (dto.getRealEstateType()) {
            case APARTMENT -> new Apartment();
            case HOUSE -> new House();
            case LAND -> new Land();
        };

        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setStatus(dto.getStatus() != null ? dto.getStatus() : Status.AVAILABLE);
        entity.setUsableArea(dto.getUsableArea());
        entity.setContractType(dto.getContractType());
        entity.setPriceDisclosure(dto.getPriceDisclosure());
        entity.setCommission(dto.getCommission());
        entity.setTaxes(dto.getTaxes());
        entity.setAvailableFrom(dto.getAvailableFrom());
        entity.setBasement(dto.isBasement());

        entity.setBuildingProperties(dto.getBuildingProperties());
        entity.setEquipment(dto.getEquipment());
        entity.setUtilities(dto.getUtilities());
        entity.setTransportPossibilities(dto.getTransportPossibilities());
        entity.setCivicAmenities(dto.getCivicAmenities());


        if (dto.getImageIds() != null) {
            if (!dto.getImageIds().isEmpty()) {
                List<Image> images = imageRepository.findAllById(dto.getImageIds());
                entity.setImage(images);
            } else {
                // If empty list is sent, clear the images
                entity.setImage(new ArrayList<>());
            }
        }

        // 4. Map Address
        entity.setAddress(dto.getAddress());

        // 5. Map Subtype Specific Fields
        if (entity instanceof Apartment apt) {
            dto.setRealEstateType(RealEstateType.APARTMENT);
            dto.setOwnershipType(apt.getOwnershipType());
            dto.setFloor(apt.getFloor());
            dto.setTotalFloors(apt.getTotalFloors());
            dto.setElevator(apt.isElevator());
            dto.setBalcony(apt.isBalcony());
            dto.setRooms(apt.getRooms());
        } else if (entity instanceof House house) {
            dto.setRealEstateType(RealEstateType.HOUSE);
            dto.setPlotArea(house.getPlotArea());
            dto.setHouseType(house.getHouseType());
            dto.setStories(house.getStories());
        } else if (entity instanceof Land land) {
            dto.setRealEstateType(RealEstateType.LAND);
            dto.setIsForHousing(land.isForHousing());
        }

        return entity;
    }

}