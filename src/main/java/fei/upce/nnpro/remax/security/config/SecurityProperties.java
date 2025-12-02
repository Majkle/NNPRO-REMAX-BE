package fei.upce.nnpro.remax.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Setter
@Getter
@Component
@ConfigurationProperties(prefix = "remax.security")
public class SecurityProperties {

    private String jwtSecret;
    private long jwtExpirationMs = 60 * 60 * 1000L;
    private String corsAllowedOrigins =  "*";
    private int failedLoginThreshold = 3;
    private long lockDurationHours = 24;
    private long passwordResetTokenExpirationMs = 10 * 60 * 1000L;
}
