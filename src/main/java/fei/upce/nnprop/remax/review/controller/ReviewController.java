package fei.upce.nnprop.remax.review.controller;

import fei.upce.nnprop.remax.review.dto.ReviewDto;
import fei.upce.nnprop.remax.review.dto.ReviewStatisticsDto;
import fei.upce.nnprop.remax.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
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
@Tag(name = "Reviews", description = "Client reviews for realtors")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Create a review", description = "Only authenticated clients can create reviews")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ReviewDto> createReview(@Valid @RequestBody ReviewDto dto, Authentication authentication) {
        ReviewDto created = reviewService.createReview(dto, authentication.getName());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(summary = "Update a review", description = "Only the author can update their review")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/{id}")
    public ResponseEntity<ReviewDto> updateReview(@PathVariable Long id,
                                                  @Valid @RequestBody ReviewDto dto,
                                                  Authentication authentication) {
        ReviewDto updated = reviewService.updateReview(id, dto, authentication.getName());
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete a review", description = "Author or Admin can delete")
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        reviewService.deleteReview(id, authentication.getName(), isAdmin);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get reviews for a specific realtor")
    @GetMapping("/realtor/{realtorId}")
    public ResponseEntity<List<ReviewDto>> getReviewsByRealtor(@PathVariable Long realtorId) {
        return ResponseEntity.ok(reviewService.getReviewsByRealtor(realtorId));
    }

    @Operation(summary = "List all reviews")
    @GetMapping("/stats/{realtorId}")
    public ResponseEntity<ReviewStatisticsDto> getRealtorStatistics(@PathVariable Long realtorId) {
        return ResponseEntity.ok(reviewService.getRealtorStatistics(realtorId));
    }

    @GetMapping
    public ResponseEntity<List<ReviewDto>> getAllReviews() {
        return ResponseEntity.ok(reviewService.listAllReviews());
    }
}
