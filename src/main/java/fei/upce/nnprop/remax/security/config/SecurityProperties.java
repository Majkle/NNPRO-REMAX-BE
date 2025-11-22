package fei.upce.nnprop.remax.security.config;

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
    private long jwtExpirationMs = 3600000;
    private String corsAllowedOrigins =  "*";
    private int failedLoginThreshold = 3;
    private long lockDurationHours = 24;
}
