package fei.upce.nnprop.remax.model.real_estates;

import fei.upce.nnprop.remax.model.real_estates.enums.UtilitiesInternetConnectionEnum;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "utilities")
public class Utilities {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "has_water", nullable = false)
    private boolean hasWater;

    @Column(name = "has_well", nullable = false)
    private boolean hasWell;

    @Column(name = "has_electricity", nullable = false)
    private boolean hasElectricity;

    @Column(name = "has_gas", nullable = false)
    private boolean hasGas;

    @Column(name = "has_sewerage", nullable = false)
    private boolean hasSewerage;

    @Column(name = "has_cesspool", nullable = false)
    private boolean hasCesspool;

    @Column(name = "has_heating", nullable = false)
    private boolean hasHeating;

    @Column(name = "has_phoneLine", nullable = false)
    private boolean hasPhoneLine;

    @Column(name = "has_cableTV", nullable = false)
    private boolean hasCableTV;

    @Column(name = "has_recycling", nullable = false)
    private boolean hasRecycling;

    @Column(name = "has_barrier_free_access", nullable = false)
    private boolean hasBarrierFreeAccess;

    @Enumerated(EnumType.STRING)
    @Column(name = "equipment", nullable = false)
    private UtilitiesInternetConnectionEnum internetConnection;

    @Column(name = "parking_places", nullable = false)
    private int parkingPlaces;

}