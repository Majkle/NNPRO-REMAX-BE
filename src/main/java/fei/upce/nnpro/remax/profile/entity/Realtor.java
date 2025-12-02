package fei.upce.nnpro.remax.profile.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@DiscriminatorValue("REALTOR")
public class Realtor extends RemaxUser {

    @Column(name = "license_number", unique = true)
    private int licenseNumber;

    @Column(name = "about", length = 2000)
    private String about;
}