package fei.upce.nnpro.remax.realestates.service;

import fei.upce.nnpro.remax.realestates.dto.RealEstateDto;
import fei.upce.nnpro.remax.realestates.entity.RealEstate;
import org.springframework.stereotype.Component;

@Component
public class RealEstateCommonUpdater {

    public void updateCommonFields(RealEstate entity, RealEstateDto dto) {
        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            entity.setDescription(dto.getDescription());
        }
        if (dto.getStatus() != null) {
            entity.setStatus(dto.getStatus());
        }
        if (dto.getUsableArea() > 0) {
            entity.setUsableArea(dto.getUsableArea());
        }
        if (dto.getContractType() != null) {
            entity.setContractType(dto.getContractType());
        }
        if (dto.getPriceDisclosure() != null) {
            entity.setPriceDisclosure(dto.getPriceDisclosure());
        }
        if (dto.getCommission() != null) {
            entity.setCommission(dto.getCommission());
        }
        if (dto.getTaxes() != null) {
            entity.setTaxes(dto.getTaxes());
        }
        if (dto.getAvailableFrom() != null) {
            entity.setAvailableFrom(dto.getAvailableFrom());
        }

        // Boolean primitive/wrapper handling
        entity.setBasement(dto.isBasement());

        // Update Embedded Objects (replacing the embedded instance is safe in JPA)
        if (dto.getBuildingProperties() != null) {
            entity.setBuildingProperties(dto.getBuildingProperties());
        }
        if (dto.getEquipment() != null) {
            entity.setEquipment(dto.getEquipment());
        }
        if (dto.getUtilities() != null) {
            entity.setUtilities(dto.getUtilities());
        }
        if (dto.getTransportPossibilities() != null) {
            entity.setTransportPossibilities(dto.getTransportPossibilities());
        }
        if (dto.getCivicAmenities() != null) {
            entity.setCivicAmenities(dto.getCivicAmenities());
        }
    }
}