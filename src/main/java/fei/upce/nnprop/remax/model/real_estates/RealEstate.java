package fei.upce.nnprop.remax.model.real_estates;

import fei.upce.nnprop.remax.model.Image;
import fei.upce.nnprop.remax.model.real_estates.enums.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "real_estate")
public abstract class RealEstate {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    @Column(name = "listed_at", nullable = false)
    private ZonedDateTime listedAt = ZonedDateTime.now();

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false, length = 4000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RealEstateStatusEnum status;

    @Column(name = "usableArea", nullable = false)
    private double usableArea;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", nullable = false)
    private RealEstateContractTypeEnum contractType;

    @Column(name = "price")
    private double price;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_disclosure", nullable = false)
    private RealEstatePriceDisclosureEnum priceDisclosure;

    @Enumerated(EnumType.STRING)
    @Column(name = "commission", nullable = false)
    private RealEstateCommissionEnum commission;

    @Enumerated(EnumType.STRING)
    @Column(name = "taxes", nullable = false)
    private RealEstateTaxesEnum taxes;

    @OneToOne(optional = false, orphanRemoval = true)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @Column(name = "available_from")
    private ZonedDateTime availableFrom;

    @OneToOne(optional = false, orphanRemoval = true)
    @JoinColumn(name = "building_properties_id", nullable = false)
    private BuildingProperties buildingProperties;

    @Enumerated(EnumType.STRING)
    @Column(name = "equipment", nullable = false)
    private RealEstateEquipmentEnum equipment;

    @OneToOne(optional = false, orphanRemoval = true)
    @JoinColumn(name = "utilities_id", nullable = false)
    private Utilities utilities;

    @OneToOne(optional = false, orphanRemoval = true)
    @JoinColumn(name = "transport_possibilities_id", nullable = false)
    private TransportPossibilities transportPossibilities;

    @OneToOne(optional = false, orphanRemoval = true)
    @JoinColumn(name = "civic_anemities_id", nullable = false)
    private CivicAnemities civicAnemities;

    @Column(name = "basement", nullable = false)
    private boolean basement;
    
    @OneToMany(orphanRemoval = true)
    @JoinColumn(name = "image", nullable = false)
    private List<Image> image;
}