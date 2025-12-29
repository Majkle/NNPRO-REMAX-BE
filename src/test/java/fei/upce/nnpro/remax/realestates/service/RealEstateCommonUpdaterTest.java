package fei.upce.nnpro.remax.realestates.service;

import fei.upce.nnpro.remax.realestates.dto.RealEstateDto;
import fei.upce.nnpro.remax.realestates.entity.Apartment;
import fei.upce.nnpro.remax.realestates.entity.BuildingProperties;
import fei.upce.nnpro.remax.realestates.entity.RealEstate;
import fei.upce.nnpro.remax.realestates.entity.enums.ContractType;
import fei.upce.nnpro.remax.realestates.entity.enums.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RealEstateCommonUpdaterTest {

    private RealEstateCommonUpdater updater;

    @BeforeEach
    void setUp() {
        updater = new RealEstateCommonUpdater();
    }

    @Test
    @DisplayName("Update: Should update fields present in DTO")
    void updateCommonFields_UpdatesValues() {
        RealEstate entity = new Apartment();
        entity.setName("Old Name");
        entity.setUsableArea(50.0);
        entity.setContractType(ContractType.SALE);

        RealEstateDto dto = new RealEstateDto();
        dto.setName("New Name");
        dto.setDescription("New Description");
        dto.setUsableArea(100.0);
        dto.setContractType(ContractType.RENTAL);
        dto.setBasement(true); // Primitive boolean

        updater.updateCommonFields(entity, dto);

        assertThat(entity.getName()).isEqualTo("New Name");
        assertThat(entity.getDescription()).isEqualTo("New Description");
        assertThat(entity.getUsableArea()).isEqualTo(100.0);
        assertThat(entity.getContractType()).isEqualTo(ContractType.RENTAL);
        assertThat(entity.isBasement()).isTrue();
    }

    @Test
    @DisplayName("Update: Should ignore null fields in DTO (Partial Update)")
    void updateCommonFields_IgnoreNulls() {
        RealEstate entity = new Apartment();
        entity.setName("Original Name");
        entity.setDescription("Original Desc");
        entity.setStatus(Status.AVAILABLE);

        RealEstateDto dto = new RealEstateDto();
        dto.setName(null); // Should keep original
        dto.setDescription(null); // Should keep original
        // Status not set -> null

        updater.updateCommonFields(entity, dto);

        assertThat(entity.getName()).isEqualTo("Original Name");
        assertThat(entity.getDescription()).isEqualTo("Original Desc");
        assertThat(entity.getStatus()).isEqualTo(Status.AVAILABLE);
    }

    @Test
    @DisplayName("Update: Should update embedded objects")
    void updateCommonFields_EmbeddedObjects() {
        RealEstate entity = new Apartment();
        entity.setBuildingProperties(null);

        RealEstateDto dto = new RealEstateDto();
        BuildingProperties props = new BuildingProperties();
        dto.setBuildingProperties(props);

        updater.updateCommonFields(entity, dto);

        assertThat(entity.getBuildingProperties()).isEqualTo(props);
    }

    @Test
    @DisplayName("Update: Should update Usable Area only if positive")
    void updateCommonFields_UsableAreaValidation() {
        RealEstate entity = new Apartment();
        entity.setUsableArea(50.0);

        RealEstateDto dto = new RealEstateDto();
        dto.setUsableArea(0.0); // Invalid/Default 0 from primitive double in some contexts, or explicit 0

        updater.updateCommonFields(entity, dto);

        assertThat(entity.getUsableArea()).isEqualTo(50.0);

        dto.setUsableArea(60.0);
        updater.updateCommonFields(entity, dto);
        assertThat(entity.getUsableArea()).isEqualTo(60.0);
    }
}
