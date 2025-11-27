package fei.upce.nnprop.remax.model.users;

import lombok.Data;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

@Data
public class RemaxUserResponse {
    private Long id;

    private String username;

    private String password;

    private String email;

    private OffsetDateTime createdAt;

    private PersonalInformation personalInformation;

    private Boolean isBlocked;

    private ZonedDateTime blockedUntil;

    private String role;

    public static String getRole(RemaxUser user) {
        if (user instanceof Admin) {
            return "ADMIN";
        }
        if (user instanceof Realtor) {
            return "AGENT";
        }
        return "CLIENT";
    }

    public static RemaxUserResponse createFrom(RemaxUser user) {
        RemaxUserResponse rur = new RemaxUserResponse();
        rur.setId(user.getId());
        rur.setUsername(user.getUsername());
        rur.setEmail(user.getEmail());
        rur.setPassword(user.getPassword());
        rur.setCreatedAt(user.getCreatedAt());
        rur.setBlockedUntil(user.getBlockedUntil());
        rur.setIsBlocked(user.getBlockedUntil() != null);
        rur.setPersonalInformation(user.getPersonalInformation());
        rur.setRole(getRole(user));
        return rur;
    }
}
