package fei.upce.nnprop.remax.model.realestates;

import fei.upce.nnprop.remax.model.realestates.enums.APARTMENT_OWNERSHIP_TYPE;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Apartment extends RealEstate {

    @Enumerated(EnumType.STRING)
    @Column(name = "ownership", nullable = false)
    private APARTMENT_OWNERSHIP_TYPE ownershipType;

    @Column(name = "floor", nullable = false)
    private int floor;

    @Column(name = "total_floors", nullable = false)
    private int totalFloors;

    @Column(name = "elevator", nullable = false)
    private boolean elevator;

    @Column(name = "balcony", nullable = false)
    private boolean balcony;

    @Column(name = "rooms", nullable = false)
    private int rooms;
}