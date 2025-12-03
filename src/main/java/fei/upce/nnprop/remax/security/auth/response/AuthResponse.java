package fei.upce.nnprop.remax.security.auth.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "JWT authentication response")
public class AuthResponse {
    @Schema(description = "JWT Access Token")
    private final String token;

    @Schema(description = "Expiration timestamp in milliseconds")
    private final long expiresAt;

    @Schema(description = "User role", example = "ADMIN")
    private final String role;
}

