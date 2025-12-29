package fei.upce.nnpro.remax.realestates.service;

import fei.upce.nnpro.remax.realestates.dto.RealEstateDto;
import fei.upce.nnpro.remax.realestates.entity.RealEstate;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class RealEstateCommonUpdater {

    /**
     * Orchestrates the update of common fields by delegating to logical groups.
     */
    public void updateCommonFields(RealEstate entity, RealEstateDto dto) {
        updateBasicInfo(entity, dto);
        updateFinancials(entity, dto);
        updateFeatures(entity, dto);
        updateEmbeddedDetails(entity, dto);
    }

    private void updateBasicInfo(RealEstate entity, RealEstateDto dto) {
        updateIfNotNull(dto.getName(), entity::setName);
        updateIfNotNull(dto.getDescription(), entity::setDescription);
        updateIfNotNull(dto.getStatus(), entity::setStatus);
        updateIfNotNull(dto.getAvailableFrom(), entity::setAvailableFrom);

        if (dto.getUsableArea() > 0) {
            entity.setUsableArea(dto.getUsableArea());
        }
    }

    private void updateFinancials(RealEstate entity, RealEstateDto dto) {
        updateIfNotNull(dto.getContractType(), entity::setContractType);
        updateIfNotNull(dto.getPriceDisclosure(), entity::setPriceDisclosure);
        updateIfNotNull(dto.getCommission(), entity::setCommission);
        updateIfNotNull(dto.getTaxes(), entity::setTaxes);
    }

    private void updateFeatures(RealEstate entity, RealEstateDto dto) {
        entity.setBasement(dto.isBasement());
        updateIfNotNull(dto.getEquipment(), entity::setEquipment);
    }

    private void updateEmbeddedDetails(RealEstate entity, RealEstateDto dto) {
        updateIfNotNull(dto.getBuildingProperties(), entity::setBuildingProperties);
        updateIfNotNull(dto.getUtilities(), entity::setUtilities);
        updateIfNotNull(dto.getTransportPossibilities(), entity::setTransportPossibilities);
        updateIfNotNull(dto.getCivicAmenities(), entity::setCivicAmenities);
    }

    /**
     * Generic helper to eliminate repetitive null checks.
     * DRY implementation.
     */
    private <T> void updateIfNotNull(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }
}