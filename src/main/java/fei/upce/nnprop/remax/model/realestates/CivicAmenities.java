package fei.upce.nnprop.remax.model.realestates;

import fei.upce.nnprop.remax.model.realestates.enums.CIVIC_AMENITY;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Embeddable
public class CivicAmenities {

    @ElementCollection(targetClass = CIVIC_AMENITY.class, fetch = FetchType.EAGER)
    @CollectionTable(
            name = "real_estate_civic_amenities",
            joinColumns = @JoinColumn(name = "real_estate_id")
    )
    @Column(name = "amenity", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<CIVIC_AMENITY> amenities = new HashSet<>();
}