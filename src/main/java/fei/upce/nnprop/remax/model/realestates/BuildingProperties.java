package fei.upce.nnprop.remax.model.realestates;

import fei.upce.nnprop.remax.model.realestates.enums.BUILDING_CONDITION;
import fei.upce.nnprop.remax.model.realestates.enums.BUILDING_LOCATION;
import fei.upce.nnprop.remax.model.realestates.enums.CONSTRUCTION_MATERIAL;
import fei.upce.nnprop.remax.model.realestates.enums.ENERGY_EFFICIENCY_CLASS;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class BuildingProperties {

    @Enumerated(EnumType.STRING)
    @Column(name = "construction_material", nullable = false)
    private CONSTRUCTION_MATERIAL constructionMaterial;

    @Enumerated(EnumType.STRING)
    @Column(name = "building_condition", nullable = false)
    private BUILDING_CONDITION buildingCondition;

    @Enumerated(EnumType.STRING)
    @Column(name = "energy_efficiency_class", nullable = false)
    private ENERGY_EFFICIENCY_CLASS energyEfficiencyClass;

    @Enumerated(EnumType.STRING)
    @Column(name = "building_location", nullable = false)
    private BUILDING_LOCATION buildingLocation;

    @Column(name = "is_in_protection_zone", nullable = false)
    private boolean isInProtectionZone;

}