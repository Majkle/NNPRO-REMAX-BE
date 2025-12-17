package fei.upce.nnpro.remax.security.admin;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request used by Admins to manually update a user")
public class UpdateUserRequest {
    @NotBlank
    @Email
    @Schema(description = "Unique email address", example = "smith@remax.xxx")
    private String email;

    // realtor-specific
    @NotNull
    @Schema(description = "Real estate license number (Required for Realtors)", example = "554433")
    private Integer licenseNumber;

    @Schema(description = "Bio or description (for Realtors)", example = "Senior agent with 10 years of experience.")
    private String about;

    // personal information
    @NotBlank
    @Schema(example = "Ing.")
    private String degree;

    @NotBlank
    @Schema(example = "John")
    private String firstName;

    @NotBlank
    @Schema(example = "Smith")
    private String lastName;

    @NotBlank
    @Schema(example = "+420123456789")
    private String phoneNumber;

    @NotNull
    @Schema(description = "ISO-8601 Date", example = "1985-05-15T00:00:00Z")
    private String birthDate;

    // address
    @NotBlank
    @Schema(example = "Business Avenue 10")
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

    @Schema(example = "Office 404")
    private String flatNumber;

    @NotBlank
    @Schema(description = "Region enum value", example = "PRAHA")
    private String region;
}

