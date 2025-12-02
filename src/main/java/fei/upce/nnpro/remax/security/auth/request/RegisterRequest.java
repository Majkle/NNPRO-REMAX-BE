package fei.upce.nnpro.remax.security.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    @NotBlank
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    // personal information
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private String phoneNumber;

    @NotNull
    private String birthDate; // ISO-8601 date-time string

    // address
    @NotBlank
    private String street;

    @NotBlank
    private String city;

    @NotBlank
    private String postalCode;

    @NotBlank
    private String country;

    private String flatNumber;

    @NotBlank
    private String region; // should match fei.upce.nnpro.remax.model.realestates.enums.AddressRegion
}
