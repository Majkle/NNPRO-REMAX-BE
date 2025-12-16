package fei.upce.nnpro.remax.security.admin;

import fei.upce.nnpro.remax.profile.dto.RemaxUserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
@Tag(name = "Admin Operations", description = "User management and administrative tasks. Requires ROLE_ADMIN.")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @Operation(summary = "Block a user account",
            description = "Prevents a user from logging in. If 'until' is not provided, blocks practically forever (666 years).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User blocked successfully",
                    content = @Content(schema = @Schema(implementation = RemaxUserResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not an admin", content = @Content)
    })
    @PostMapping("/block/{username}")
    public ResponseEntity<?> blockUser(@Parameter(description = "Username of the user to block") @PathVariable String username,
                                       @Parameter(description = "Optional expiration date for the block (ISO-8601)")
                                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime until) {
        if (until == null) {
            until = ZonedDateTime.now().plusYears(666);
        }
        log.info("Admin request to block user {} until {}", username, until);
        return ResponseEntity.ok(adminService.blockUser(username, until));
    }

    @Operation(summary = "Unblock a user account", description = "Restores login access to a previously blocked user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User unblocked successfully",
                    content = @Content(schema = @Schema(implementation = RemaxUserResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @PostMapping("/unblock/{username}")
    public ResponseEntity<?> unblockUser(
            @Parameter(description = "Username of the user to unblock") @PathVariable String username) {
        log.info("Admin request to unblock user {}", username);
        return ResponseEntity.ok(adminService.unblockUser(username));
    }

    @Operation(summary = "List all users", description = "Retrieves a list of all registered users (Admins, Realtors, Clients).")
    @ApiResponse(responseCode = "200", description = "List of users retrieved")
    @GetMapping("/users")
    public ResponseEntity<List<RemaxUserResponse>> getAllUsers() {
        log.info("Admin request to list all users");
        return ResponseEntity.ok(adminService.listAllUsers());
    }

    @Operation(summary = "Delete a user", description = "Permanently deletes a user account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @DeleteMapping("/users/{username}")
    public ResponseEntity<?> deleteUser(@Parameter(description = "Username of the user to delete") @PathVariable String username) {
        log.info("Admin request to delete user {}", username);
        adminService.deleteUser(username);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Create a Realtor", description = "Manually registers a new Realtor account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Realtor created successfully",
                    content = @Content(schema = @Schema(implementation = RemaxUserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Username or Email already exists",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @PostMapping("/realtors")
    public ResponseEntity<?> createRealtor(@RequestBody CreateUserRequest request) {
        log.info("Admin request to create realtor {}", request.getUsername());
        return ResponseEntity.ok(adminService.createRealtor(request));
    }

    @Operation(summary = "Create an Admin", description = "Manually registers a new Administrator account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Admin created successfully",
                    content = @Content(schema = @Schema(implementation = RemaxUserResponse.class))),
            @ApiResponse(responseCode = "400", description = "Username or Email already exists",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @PostMapping("/admins")
    public ResponseEntity<?> createAdmin(@RequestBody CreateUserRequest request) {
        log.info("Admin request to create admin {}", request.getUsername());
        return ResponseEntity.ok(adminService.createAdmin(request));
    }
}
