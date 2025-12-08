package fei.upce.nnpro.remax.profile.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Profile update data")
public class ProfileUpdateRequest {
    // personal information
    @NotBlank
    @Schema(example = "Jan")
    private String firstName;

    @NotBlank
    @Schema(example = "Novak")
    private String lastName;

    @NotBlank
    @Schema(example = "+420987654321")
    private String phoneNumber;

    @NotNull
    @Schema(description = "ISO-8601 Date", example = "1990-01-01T00:00:00Z")
    private String birthDate;

    // address
    @NotBlank
    @Schema(example = "New Street 10")
    private String street;

    @NotBlank
    @Schema(example = "Brno")
    private String city;

    @NotBlank
    @Schema(example = "60200")
    private String postalCode;

    @NotBlank
    @Schema(example = "Czech Republic")
    private String country;

    @Schema(example = "5")
    private String flatNumber;

    @NotBlank
    @Schema(example = "JIHOMORAVSKY")
    private String region;
}

