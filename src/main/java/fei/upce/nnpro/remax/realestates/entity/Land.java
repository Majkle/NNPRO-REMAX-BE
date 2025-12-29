package fei.upce.nnpro.remax.realestates.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@Entity
@SuperBuilder
@NoArgsConstructor
public class Land extends RealEstate {

    @Column(name = "is_for_housing", nullable = false)
    private boolean isForHousing;

}