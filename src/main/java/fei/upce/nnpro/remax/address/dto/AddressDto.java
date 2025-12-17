package fei.upce.nnpro.remax.address.dto;

import fei.upce.nnpro.remax.realestates.entity.enums.AddressRegion;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Data Transfer Object for Address")
public class AddressDto {

    @Schema(description = "ID of the address (read-only)", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank(message = "Street is required")
    @Size(max = 100)
    @Schema(description = "Street name and number", example = "Václavské náměstí 1")
    private String street;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    @Schema(description = "City name", example = "Praha")
    private String city;

    @NotBlank(message = "Postal code is required")
    @Schema(description = "Postal/Zip code", example = "110 00")
    private String postalCode;

    @NotBlank(message = "Country is required")
    @Schema(description = "Country name", example = "Česká republika")
    private String country;

    @Schema(description = "Flat/Apartment number", example = "4B")
    private String flatNumber;

    @NotNull(message = "Region is required")
    @Schema(description = "Region/State", example = "PRAHA")
    private AddressRegion region;

    @Schema(description = "GPS Latitude", example = "50.0835")
    private Double latitude;

    @Schema(description = "GPS Longitude", example = "14.4341")
    private Double longitude;
}