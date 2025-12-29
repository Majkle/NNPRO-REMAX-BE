package fei.upce.nnpro.remax.realestates.entity;

import fei.upce.nnpro.remax.realestates.entity.enums.ApartmentOwnershipType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
public class Apartment extends RealEstate {

    @Enumerated(EnumType.STRING)
    @Column(name = "ownership", nullable = false)
    private ApartmentOwnershipType ownershipType;

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