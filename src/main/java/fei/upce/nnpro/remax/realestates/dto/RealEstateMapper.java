package fei.upce.nnpro.remax.realestates.dto;

import fei.upce.nnpro.remax.images.entity.Image;
import fei.upce.nnpro.remax.images.repository.ImageRepository;
import fei.upce.nnpro.remax.realestates.entity.*;
import fei.upce.nnpro.remax.realestates.entity.enums.RealEstateType;
import fei.upce.nnpro.remax.realestates.entity.enums.Status;
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

    /**
     * Maps DTO data to an Entity.
     * If 'existingEntity' is null, creates a new instance based on type.
     * If 'existingEntity' is provided, updates it.
     */
    public RealEstate toEntity(RealEstateDto dto) {
        RealEstate entity = switch (dto.getType()) {
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


        if (dto.getImages() != null) {
            if (!dto.getImages().isEmpty()) {
                List<Image> images = imageRepository.findAllById(dto.getImages());
                entity.setImages(images);
            } else {
                // If empty list is sent, clear the images
                entity.setImages(new ArrayList<>());
            }
        }

        // 4. Map Address
        entity.setAddress(dto.getAddress());

        // 5. Map Subtype Specific Fields
        if (entity instanceof Apartment apt) {
            apt.setOwnershipType(dto.getOwnershipType());
            if (dto.getFloor() != null) apt.setFloor(dto.getFloor());
            if (dto.getTotalFloors() != null) apt.setTotalFloors(dto.getTotalFloors());
            if (dto.getElevator() != null) apt.setElevator(dto.getElevator());
            if (dto.getBalcony() != null) apt.setBalcony(dto.getBalcony());
            if (dto.getRooms() != null) apt.setRooms(dto.getRooms());
        } else if (entity instanceof House house) {
            if (dto.getPlotArea() != null) house.setPlotArea(dto.getPlotArea());
            house.setHouseType(dto.getHouseType());
            if (dto.getStories() != null) house.setStories(dto.getStories());
        } else if (entity instanceof Land land) {
            if (dto.getIsForHousing() != null) land.setForHousing(dto.getIsForHousing());
        }

        return entity;
    }

}