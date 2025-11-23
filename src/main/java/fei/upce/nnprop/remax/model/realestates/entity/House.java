package fei.upce.nnprop.remax.model.realestates.entity;

import fei.upce.nnprop.remax.model.realestates.enums.HouseType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class House extends RealEstate {

    @Column(name = "plot_area", nullable = false)
    private double plotArea;

    @Enumerated(EnumType.STRING)
    @Column(name = "house_type", nullable = false)
    private HouseType houseType;

    @Column(name = "stories", nullable = false)
    private int stories;
}