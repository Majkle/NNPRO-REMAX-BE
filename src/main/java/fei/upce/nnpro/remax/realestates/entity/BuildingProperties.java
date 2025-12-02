package fei.upce.nnpro.remax.realestates.entity;

import fei.upce.nnpro.remax.realestates.entity.enums.BuildingCondition;
import fei.upce.nnpro.remax.realestates.entity.enums.BuildingLocation;
import fei.upce.nnpro.remax.realestates.entity.enums.ConstructionMaterial;
import fei.upce.nnpro.remax.realestates.entity.enums.EnergyEfficiencyClass;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class BuildingProperties {

    @Enumerated(EnumType.STRING)
    @Column(name = "construction_material", nullable = false)
    private ConstructionMaterial constructionMaterial;

    @Enumerated(EnumType.STRING)
    @Column(name = "building_condition", nullable = false)
    private BuildingCondition buildingCondition;

    @Enumerated(EnumType.STRING)
    @Column(name = "energy_efficiency_class", nullable = false)
    private EnergyEfficiencyClass energyEfficiencyClass;

    @Enumerated(EnumType.STRING)
    @Column(name = "building_location", nullable = false)
    private BuildingLocation buildingLocation;

    @Column(name = "is_in_protection_zone", nullable = false)
    private boolean isInProtectionZone;

}