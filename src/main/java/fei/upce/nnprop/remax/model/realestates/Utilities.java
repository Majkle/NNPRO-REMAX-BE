package fei.upce.nnprop.remax.model.realestates;

import fei.upce.nnprop.remax.model.realestates.enums.INTERNET_CONNECTION_TYPE;
import fei.upce.nnprop.remax.model.realestates.enums.UTILITY_TYPE;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Embeddable
public class Utilities {

    @ElementCollection(targetClass = UTILITY_TYPE.class, fetch = FetchType.EAGER)
    @CollectionTable(
            name = "real_estate_utilities",
            joinColumns = @JoinColumn(name = "real_estate_id")
    )
    @Column(name = "utility", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<UTILITY_TYPE> availableUtilities = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "internet_connection", nullable = false)
    private INTERNET_CONNECTION_TYPE internetConnection;

    @Column(name = "parking_places", nullable = false)
    private int parkingPlaces;

}