package fei.upce.nnprop.remax.model.real_estates;

import fei.upce.nnprop.remax.model.real_estates.enums.BuildingPropertiesBuildingConditionEnum;
import fei.upce.nnprop.remax.model.real_estates.enums.BuildingPropertiesBuildingLocationEnum;
import fei.upce.nnprop.remax.model.real_estates.enums.BuildingPropertiesConstructionMaterialEnum;
import fei.upce.nnprop.remax.model.real_estates.enums.BuildingPropertiesenergyEfficiencyClassEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "building_properties")
public class BuildingProperties {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "construction_material", nullable = false)
    private BuildingPropertiesConstructionMaterialEnum constructionMaterial;

    @Enumerated(EnumType.STRING)
    @Column(name = "building_condition", nullable = false)
    private BuildingPropertiesBuildingConditionEnum buildingCondition;

    @Enumerated(EnumType.STRING)
    @Column(name = "energy_efficiency_class", nullable = false)
    private BuildingPropertiesenergyEfficiencyClassEnum energyEfficiencyClass;

    @Enumerated(EnumType.STRING)
    @Column(name = "building_location", nullable = false)
    private BuildingPropertiesBuildingLocationEnum buildingLocation;

    @Column(name = "is_in_protection_zone", nullable = false)
    private boolean isInProtectionZone;

}