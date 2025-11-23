package fei.upce.nnprop.remax.model.realestates.entity;

import fei.upce.nnprop.remax.model.realestates.enums.CivicAmenity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Embeddable
public class CivicAmenities {

    @ElementCollection(targetClass = CivicAmenity.class, fetch = FetchType.LAZY)
    @CollectionTable(
            name = "real_estate_civic_amenities",
            joinColumns = @JoinColumn(name = "real_estate_id")
    )
    @Column(name = "amenity", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<CivicAmenity> amenities = new HashSet<>();
}