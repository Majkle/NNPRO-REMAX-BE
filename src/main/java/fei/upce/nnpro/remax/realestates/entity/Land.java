package fei.upce.nnpro.remax.realestates.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Land extends RealEstate {

    @Column(name = "is_for_housing", nullable = false)
    private boolean isForHousing;

}