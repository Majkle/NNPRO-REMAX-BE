package fei.upce.nnpro.remax.security.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Schema(description = "Client registration request")
public class RegisterRequest {
    @NotBlank
    @Schema(example = "jan_novak")
    private String username;

    @NotBlank
    @Email
    @Schema(example = "jan@example.com")
    private String email;

    @NotBlank
    @Schema(example = "password123")
    private String password;

    // personal information
    @NotBlank
    @Schema(example = "Jan")
    private String firstName;

    @NotBlank
    @Schema(example = "Novak")
    private String lastName;

    @NotBlank
    @Schema(example = "+420123456789")
    private String phoneNumber;

    @NotNull
    @Schema(description = "ISO-8601 Date", example = "1990-01-01T00:00:00Z")
    private String birthDate; // ISO-8601 date-time string

    // address
    @NotBlank
    @Schema(example = "Main Street 1")
    private String street;

    @NotBlank
    @Schema(example = "Prague")
    private String city;

    @NotBlank
    @Schema(example = "11000")
    private String postalCode;

    @NotBlank
    @Schema(example = "Czech Republic")
    private String country;

    @Schema(example = "3B")
    private String flatNumber;

    @NotBlank
    @Schema(description = "Region enum value", example = "PRAHA")
    private String region; // should match fei.upce.nnpro.remax.model.realestates.enums.AddressRegion

    @Schema(description = "Image ID associated with this profile", example = "1")
    private Long image;
}
