package fei.upce.nnpro.remax.profile.dto;

import fei.upce.nnpro.remax.profile.entity.Admin;
import fei.upce.nnpro.remax.profile.entity.PersonalInformation;
import fei.upce.nnpro.remax.profile.entity.Realtor;
import fei.upce.nnpro.remax.profile.entity.RemaxUser;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

@Data
@Schema(description = "Response object representing a user (Admin, Agent, or Client) and their status")
public class RemaxUserResponse {

    @Schema(description = "Unique identifier of the user", example = "10")
    private Long id;

    @Schema(description = "Unique username used for login", example = "jan_novak")
    private String username;

    @Schema(description = "Masked password", example = "********")
    private String password;

    @Schema(description = "User's email address", example = "jan.novak@example.com")
    private String email;

    @Schema(description = "Timestamp when the account was created", example = "2024-01-01T12:00:00+01:00")
    private OffsetDateTime createdAt;

    @Schema(description = "Detailed personal information (Name, Address, Phone)")
    private PersonalInformation personalInformation;

    @Schema(description = "Indicates if the account is currently blocked", example = "false")
    private Boolean isBlocked;

    @Schema(description = "Timestamp until when the user is blocked. Null if account is active.", example = "2025-12-31T23:59:59Z")
    private ZonedDateTime blockedUntil;

    @Schema(description = "The role derived from the user type", example = "CLIENT", allowableValues = {"ADMIN", "AGENT", "CLIENT"})
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
