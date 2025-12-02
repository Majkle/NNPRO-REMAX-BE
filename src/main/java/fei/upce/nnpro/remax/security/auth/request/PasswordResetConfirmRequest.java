package fei.upce.nnpro.remax.security.auth.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordResetConfirmRequest {
    @NotBlank
    private String username;
    @NotBlank
    private String code;
    @NotBlank
    private String newPassword;
}

