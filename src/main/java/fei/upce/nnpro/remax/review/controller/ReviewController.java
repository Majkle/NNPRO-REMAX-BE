package fei.upce.nnpro.remax.review.controller;

import fei.upce.nnpro.remax.review.dto.RealtorSimplifiedDto;
import fei.upce.nnpro.remax.review.dto.ReviewDto;
import fei.upce.nnpro.remax.review.dto.ReviewStatisticsDto;
import fei.upce.nnpro.remax.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Management of ratings and reviews for Realtors")
@SecurityRequirement(name = "bearerAuth")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Create a review", description = "Allows a Client to review a Realtor. Realtors cannot review other Realtors.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Review created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or Author is not a Client", content = @Content),
            @ApiResponse(responseCode = "404", description = "Target Realtor not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReviewDto> createReview(@Valid @RequestBody ReviewDto dto, Authentication authentication) {
        ReviewDto created = reviewService.createReview(dto, authentication.getName());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(summary = "Update a review", description = "Updates an existing review. Only the original author can update their review.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Review updated successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not the author", content = @Content),
            @ApiResponse(responseCode = "404", description = "Review not found", content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReviewDto> updateReview(@Parameter(description = "ID of the review to update") @PathVariable Long id,
                                                  @Valid @RequestBody ReviewDto dto,
                                                  Authentication authentication) {
        ReviewDto updated = reviewService.updateReview(id, dto, authentication.getName());
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete a review", description = "Deletes a review. Can be performed by the original Author or an Admin.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Review deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User is not the author or Admin", content = @Content),
            @ApiResponse(responseCode = "404", description = "Review not found", content = @Content)
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> deleteReview(
            @Parameter(description = "ID of the review to delete") @PathVariable Long id,
            Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        reviewService.deleteReview(id, authentication.getName(), isAdmin);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get reviews for a Realtor", description = "Retrieves a list of all reviews assigned to a specific Realtor.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of reviews retrieved")
    })
    @GetMapping("/realtor/{realtorId}")
        public ResponseEntity<List<ReviewDto>> getReviewsByRealtor(@Parameter(description = "ID of the realtor") @PathVariable Long realtorId) {
        return ResponseEntity.ok(reviewService.getReviewsByRealtor(realtorId));
    }

    @Operation(summary = "Get all Realtors", description = "Retrieves a list of all all Realtors.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of Realtors retrieved")
    })
    @GetMapping("/realtors")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<List<RealtorSimplifiedDto>> getAllRealtors(@Parameter(hidden = true) Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(reviewService.getAllRealtors());
    }

    @Operation(summary = "Get Realtor statistics", description = "Calculates aggregated statistics (average ratings) for a specific Realtor.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Realtor not found", content = @Content)
    })
    @GetMapping("/stats/{realtorId}")
    public ResponseEntity<ReviewStatisticsDto> getRealtorStatistics(@Parameter(description = "ID of the realtor") @PathVariable Long realtorId) {
        return ResponseEntity.ok(reviewService.getRealtorStatistics(realtorId));
    }

    @Operation(summary = "List all reviews", description = "Retrieves all reviews in the system.")
    @ApiResponse(responseCode = "200", description = "List of all reviews")
    @GetMapping
    public ResponseEntity<List<ReviewDto>> getAllReviews() {
        return ResponseEntity.ok(reviewService.listAllReviews());
    }
}
