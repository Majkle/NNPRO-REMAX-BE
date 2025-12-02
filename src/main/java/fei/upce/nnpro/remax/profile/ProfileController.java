package fei.upce.nnpro.remax.profile;

import fei.upce.nnpro.remax.model.users.RemaxUser;
import fei.upce.nnpro.remax.profile.request.ProfileUpdateRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private static final Logger log = LoggerFactory.getLogger(ProfileController.class);

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<?> getProfile(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        String username = authentication.getName();
        log.info("GET /api/profile for {}", username);
        return profileService.getProfile(username)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PatchMapping
    public ResponseEntity<?> updateProfile(Authentication authentication, @Valid @RequestBody ProfileUpdateRequest request) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        String username = authentication.getName();
        log.info("PATCH /api/profile for {}", username);
        try {
            RemaxUser updated = profileService.updateProfile(username, request);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity<?> deleteProfile(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        String username = authentication.getName();
        log.info("DELETE /api/profile for {}", username);
        try {
            profileService.deleteProfile(username);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
