package fei.upce.nnprop.remax.model.realestates;

import fei.upce.nnprop.remax.model.Image;
import fei.upce.nnprop.remax.model.realestates.enums.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "real_estate")
@Inheritance(strategy = InheritanceType.JOINED)
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
    private Status status;

    @Column(name = "usableArea", nullable = false)
    private double usableArea;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", nullable = false)
    private ContractType contractType;

    @OneToMany(orphanRemoval = true)
    @JoinColumn(name = "price_history_id", nullable = false)
    private List<PriceHistory> priceHistory;

    @Enumerated(EnumType.STRING)
    @Column(name = "price_disclosure", nullable = false)
    private PriceDisclosure priceDisclosure;

    @Enumerated(EnumType.STRING)
    @Column(name = "commission", nullable = false)
    private Commission commission;

    @Enumerated(EnumType.STRING)
    @Column(name = "taxes", nullable = false)
    private Taxes taxes;

    @OneToOne(optional = false, orphanRemoval = true)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @Column(name = "available_from")
    private ZonedDateTime availableFrom;

    @Embedded
    private BuildingProperties buildingProperties;

    @Enumerated(EnumType.STRING)
    @Column(name = "equipment", nullable = false)
    private Equipment equipment;

    @Embedded
    private Utilities utilities;

    @Embedded
    private TransportPossibilities transportPossibilities;

    @Embedded
    private CivicAmenities civicAmenities;

    @Column(name = "basement", nullable = false)
    private boolean basement;
    
    @OneToMany(orphanRemoval = true)
    @JoinColumn(name = "real_estate_id", nullable = false)
    private List<Image> image;
}