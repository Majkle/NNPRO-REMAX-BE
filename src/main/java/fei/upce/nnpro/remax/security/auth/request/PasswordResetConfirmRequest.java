package fei.upce.nnpro.remax.security.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Request object for confirming password reset using a verification code")
public class PasswordResetConfirmRequest {

    @NotBlank
    @Schema(description = "The username of the account", example = "john_doe")
    private String username;

    @NotBlank
    @Schema(description = "The reset code received via email", example = "8F3D2A")
    private String code;

    @NotBlank
    @Schema(description = "The new password to set", example = "NewSecurePassword123!")
    private String newPassword;
}

