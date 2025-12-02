package fei.upce.nnpro.remax.realestates.entity;

import fei.upce.nnpro.remax.realestates.entity.enums.InternetConnectionType;
import fei.upce.nnpro.remax.realestates.entity.enums.UtilityType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Embeddable
public class Utilities {

    @ElementCollection(targetClass = UtilityType.class, fetch = FetchType.LAZY)
    @CollectionTable(
            name = "real_estate_utilities",
            joinColumns = @JoinColumn(name = "real_estate_id")
    )
    @Column(name = "utility", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<UtilityType> availableUtilities = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "internet_connection", nullable = false)
    private InternetConnectionType internetConnection;

    @Column(name = "parking_places", nullable = false)
    private int parkingPlaces;

}