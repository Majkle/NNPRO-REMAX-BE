package fei.upce.nnpro.remax.security.admin;

import fei.upce.nnpro.remax.model.users.RemaxUserResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/block/{username}")
    public ResponseEntity<?> blockUser(@PathVariable String username,
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime until) {
        if (until == null) {
            // default: block indefinitely (set some far future date)
            until = ZonedDateTime.now().plusYears(100);
        }
        log.info("Admin request to block user {} until {}", username, until);
        return ResponseEntity.ok(adminService.blockUser(username, until));
    }

    @PostMapping("/unblock/{username}")
    public ResponseEntity<?> unblockUser(@PathVariable String username) {
        log.info("Admin request to unblock user {}", username);
        return ResponseEntity.ok(adminService.unblockUser(username));
    }

    @GetMapping("/users")
    public ResponseEntity<List<RemaxUserResponse>> getAllUsers() {
        log.info("Admin request to list all users");
        return ResponseEntity.ok(adminService.listAllUsers());
    }

    @DeleteMapping("/users/{username}")
    public ResponseEntity<?> deleteUser(@PathVariable String username) {
        log.info("Admin request to delete user {}", username);
        adminService.deleteUser(username);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/realtors")
    public ResponseEntity<?> createRealtor(@RequestBody CreateUserRequest request) {
        log.info("Admin request to create realtor {}", request.getUsername());
        return ResponseEntity.ok(adminService.createRealtor(request));
    }

    @PostMapping("/admins")
    public ResponseEntity<?> createAdmin(@RequestBody CreateUserRequest request) {
        log.info("Admin request to create admin {}", request.getUsername());
        return ResponseEntity.ok(adminService.createAdmin(request));
    }
}
