package fei.upce.nnprop.remax.model.realestates;

import fei.upce.nnprop.remax.model.realestates.enums.TransportPossibility;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Embeddable
public class TransportPossibilities {

    @ElementCollection(targetClass = TransportPossibility.class, fetch = FetchType.EAGER)
    @CollectionTable(
            name = "real_estate_transport_possibilities",
            joinColumns = @JoinColumn(name = "real_estate_id")
    )
    @Column(name = "possibility", nullable = false)
    @Enumerated(EnumType.STRING)
    private Set<TransportPossibility> possibilities = new HashSet<>();
}