package fei.upce.nnpro.remax.security.auth.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {
    private final String token;
    private final long expiresAt;
    private final String role;
}

