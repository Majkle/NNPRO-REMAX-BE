package fei.upce.nnprop.remax.security.admin;

import fei.upce.nnprop.remax.model.users.RemaxUserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Administrative operations (User management)")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @Operation(summary = "Block user", description = "Blocks a user account until a specific date")
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

    @Operation(summary = "Unblock user")
    @PostMapping("/unblock/{username}")
    public ResponseEntity<?> unblockUser(@PathVariable String username) {
        log.info("Admin request to unblock user {}", username);
        return ResponseEntity.ok(adminService.unblockUser(username));
    }

    @Operation(summary = "List all users")
    @GetMapping("/users")
    public ResponseEntity<List<RemaxUserResponse>> getAllUsers() {
        log.info("Admin request to list all users");
        return ResponseEntity.ok(adminService.listAllUsers());
    }

    @Operation(summary = "Delete user")
    @DeleteMapping("/users/{username}")
    public ResponseEntity<?> deleteUser(@PathVariable String username) {
        log.info("Admin request to delete user {}", username);
        adminService.deleteUser(username);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Create Realtor", description = "Creates a new user with Realtor role")
    @PostMapping("/realtors")
    public ResponseEntity<?> createRealtor(@RequestBody CreateUserRequest request) {
        log.info("Admin request to create realtor {}", request.getUsername());
        return ResponseEntity.ok(adminService.createRealtor(request));
    }

    @Operation(summary = "Create Admin", description = "Creates a new user with Admin role")
    @PostMapping("/admins")
    public ResponseEntity<?> createAdmin(@RequestBody CreateUserRequest request) {
        log.info("Admin request to create admin {}", request.getUsername());
        return ResponseEntity.ok(adminService.createAdmin(request));
    }
}
