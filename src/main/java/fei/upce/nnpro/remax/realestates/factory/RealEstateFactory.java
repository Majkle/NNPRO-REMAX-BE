package fei.upce.nnpro.remax.realestates.factory;

import fei.upce.nnpro.remax.realestates.dto.RealEstateDto;
import fei.upce.nnpro.remax.realestates.entity.Apartment;
import fei.upce.nnpro.remax.realestates.entity.House;
import fei.upce.nnpro.remax.realestates.entity.Land;
import fei.upce.nnpro.remax.realestates.entity.RealEstate;
import fei.upce.nnpro.remax.realestates.entity.enums.Status;
import org.springframework.stereotype.Component;

@Component
public class RealEstateFactory {

    public RealEstate createEntity(RealEstateDto dto) {
        if (dto.getType() == null) {
            throw new IllegalArgumentException("RealEstateType cannot be null");
        }

        return switch (dto.getType()) {
            case APARTMENT -> {
                var builder = Apartment.builder();
                applyCommonFields(builder, dto);
                yield builder
                        .floor(dto.getFloor() != null ? dto.getFloor()  : 0)
                        .totalFloors(dto.getTotalFloors() != null ? dto.getTotalFloors()  : 0)
                        .elevator(Boolean.TRUE.equals(dto.getElevator()))
                        .balcony(Boolean.TRUE.equals(dto.getBalcony()))
                        .rooms(dto.getRooms() != null ? dto.getRooms() : 0)
                        .ownershipType(dto.getOwnershipType())
                        .build();
            }
            case HOUSE -> {
                var builder = House.builder();
                applyCommonFields(builder, dto);
                yield builder
                        .plotArea(dto.getPlotArea() != null ? dto.getPlotArea() : 0.0)
                        .stories(dto.getStories() != null ? dto.getStories() : 0)
                        .houseType(dto.getHouseType())
                        .build();
            }
            case LAND -> {
                var builder = Land.builder();
                applyCommonFields(builder, dto);
                yield builder
                        .isForHousing(Boolean.TRUE.equals(dto.getIsForHousing()))
                        .build();
            }
        };
    }

    private void applyCommonFields(RealEstate.RealEstateBuilder<?, ?> builder, RealEstateDto dto) {
        builder
                .name(dto.getName())
                .description(dto.getDescription())
                .status(dto.getStatus() != null ? dto.getStatus() : Status.AVAILABLE)
                .usableArea(dto.getUsableArea())
                .contractType(dto.getContractType())
                .priceDisclosure(dto.getPriceDisclosure())
                .commission(dto.getCommission())
                .taxes(dto.getTaxes())
                .availableFrom(dto.getAvailableFrom())
                .basement(Boolean.TRUE.equals(dto.getBasement()))
                // Embedded Objects
                .buildingProperties(dto.getBuildingProperties())
                .equipment(dto.getEquipment())
                .utilities(dto.getUtilities())
                .transportPossibilities(dto.getTransportPossibilities())
                .civicAmenities(dto.getCivicAmenities());
    }
}