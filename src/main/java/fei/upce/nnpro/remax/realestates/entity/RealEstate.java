package fei.upce.nnpro.remax.realestates.entity;

import fei.upce.nnpro.remax.address.entity.Address;
import fei.upce.nnpro.remax.images.entity.Image;
import fei.upce.nnpro.remax.profile.entity.RemaxUser;
import fei.upce.nnpro.remax.realestates.entity.enums.*;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.ArrayList;

@Getter
@Setter
@Entity
@Table(name = "real_estate")
@SuperBuilder
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class RealEstate {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private Long id;

    @Builder.Default
    @Column(name = "listed_at", nullable = false)
    private ZonedDateTime listedAt = ZonedDateTime.now();

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", nullable = false, length = 4000)
    private String description;

    @ManyToOne(optional = false)
    @JoinColumn(name = "realtor_id", nullable = false)
    private RemaxUser realtor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "usableArea", nullable = false)
    private double usableArea;

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_type", nullable = false)
    private ContractType contractType;

    @Builder.Default
    @OneToMany(
            mappedBy = "realEstate",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PriceHistory> priceHistory = new ArrayList<>();

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

    @Builder.Default
    @OneToMany(
            mappedBy = "realEstate",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<Image> images = new ArrayList<>();
}