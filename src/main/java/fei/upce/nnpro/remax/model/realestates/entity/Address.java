package fei.upce.nnpro.remax.model.realestates.entity;

import fei.upce.nnpro.remax.model.realestates.enums.AddressRegion;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "address")
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotBlank(message = "Street is required")
    @Size(max = 100)
    @Column(name = "street", nullable = false)
    private String street;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    @Column(name = "city", nullable = false)
    private String city;

    @NotBlank(message = "Postal code is required")
    @Column(name = "postal_code", nullable = false)
    private String postalCode;

    @NotBlank(message = "Country is required")
    @Column(name = "country", nullable = false)
    private String country;

    @Column(name = "flat_number")
    private String flatNumber;

    @NotNull(message = "Region is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "region", nullable = false)
    private AddressRegion region;
}