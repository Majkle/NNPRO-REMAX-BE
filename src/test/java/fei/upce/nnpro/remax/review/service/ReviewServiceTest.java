package fei.upce.nnpro.remax.review.service;

import fei.upce.nnpro.remax.review.entity.Review;
import fei.upce.nnpro.remax.profile.entity.Client;
import fei.upce.nnpro.remax.profile.entity.PersonalInformation;
import fei.upce.nnpro.remax.profile.entity.Realtor;
import fei.upce.nnpro.remax.profile.repository.RemaxUserRepository;
import fei.upce.nnpro.remax.review.dto.ReviewMapper;
import fei.upce.nnpro.remax.review.dto.ReviewDto;
import fei.upce.nnpro.remax.review.dto.ReviewStatisticsDto;
import fei.upce.nnpro.remax.review.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private RemaxUserRepository userRepository;

    @Mock
    private ReviewMapper reviewMapper;

    @InjectMocks
    private ReviewService reviewService;

    // --------------------------------------------------------------------------------------
    // CREATE TESTS
    // --------------------------------------------------------------------------------------

    @Test
    @DisplayName("Create: Should create review when Author is Client and Realtor exists")
    void createReview_Success() {
        // Arrange
        Long realtorId = 100L;
        String username = "clientUser";

        // DTO
        ReviewDto dto = new ReviewDto();
        dto.setRealtorId(realtorId);
        dto.setText("Great job");
        dto.setOverall(5);

        // Mock Client (Author)
        Client client = new Client();
        client.setId(1L);
        client.setUsername(username);
        PersonalInformation pi = new PersonalInformation();
        pi.setFirstName("John");
        pi.setLastName("Doe");
        client.setPersonalInformation(pi);

        // Mock Realtor
        Realtor realtor = new Realtor();
        realtor.setId(realtorId);

        // Mock Entity
        Review reviewEntity = new Review();

        // Stubbing
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(client));
        when(userRepository.findById(realtorId)).thenReturn(Optional.of(realtor));
        when(reviewMapper.toEntity(dto)).thenReturn(reviewEntity);
        when(reviewRepository.save(any(Review.class))).thenReturn(reviewEntity);
        when(reviewMapper.toDto(reviewEntity)).thenReturn(dto);

        // Act
        ReviewDto result = reviewService.createReview(dto, username);

        // Assert
        assertThat(result).isNotNull();

        // Verify Author and Realtor were set on the entity before saving
        verify(reviewMapper).toEntity(dto);
        ArgumentCaptor<Review> reviewCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewRepository).save(reviewCaptor.capture());

        Review captured = reviewCaptor.getValue();
        assertThat(captured.getAuthor()).isEqualTo(client);
        assertThat(captured.getRealtor()).isEqualTo(realtor);
        assertThat(captured.getClientDisplayName()).isEqualTo("John Doe");
    }

    @Test
    @DisplayName("Create: Should throw exception if User is not a Client (e.g., Realtor trying to review)")
    void createReview_Fail_NotAClient() {
        // Arrange
        String username = "realtorUser";
        ReviewDto dto = new ReviewDto();
        dto.setRealtorId(2L);

        Realtor actingUser = new Realtor(); // actingUser is a REALTOR, not CLIENT
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(actingUser));

        // Act & Assert
        assertThatThrownBy(() -> reviewService.createReview(dto, username))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Only Clients can write reviews");
    }

    @Test
    @DisplayName("Create: Should throw exception if Target Realtor does not exist")
    void createReview_Fail_RealtorNotFound() {
        // Arrange
        String username = "clientUser";
        ReviewDto dto = new ReviewDto();
        dto.setRealtorId(999L);

        Client client = new Client();
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(client));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> reviewService.createReview(dto, username))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Realtor not found");
    }

    // --------------------------------------------------------------------------------------
    // UPDATE TESTS
    // --------------------------------------------------------------------------------------

    @Test
    @DisplayName("Update: Should update fields if User is the Author")
    void updateReview_Success() {
        // Arrange
        Long reviewId = 10L;
        String username = "authorUser";

        ReviewDto dto = new ReviewDto();
        dto.setOverall(4);
        dto.setText("Updated Text");

        Client author = new Client();
        author.setUsername(username);

        Review existingReview = new Review();
        existingReview.setId(reviewId);
        existingReview.setAuthor(author);
        existingReview.setOverall(1);
        existingReview.setText("Old Text");

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(existingReview));
        when(reviewRepository.save(existingReview)).thenReturn(existingReview);
        when(reviewMapper.toDto(existingReview)).thenReturn(dto);

        // Act
        reviewService.updateReview(reviewId, dto, username);

        // Assert
        verify(reviewRepository).save(existingReview);
        assertThat(existingReview.getOverall()).isEqualTo(4);
        assertThat(existingReview.getText()).isEqualTo("Updated Text");
    }

    @Test
    @DisplayName("Update: Should throw AccessDenied if User is NOT the Author")
    void updateReview_Fail_NotAuthor() {
        // Arrange
        Long reviewId = 10L;
        String username = "hacker";

        Client realAuthor = new Client();
        realAuthor.setUsername("realAuthor");

        Review existingReview = new Review();
        existingReview.setId(reviewId);
        existingReview.setAuthor(realAuthor);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(existingReview));

        // Act & Assert
        assertThatThrownBy(() -> reviewService.updateReview(reviewId, new ReviewDto(), username))
                .isInstanceOf(AccessDeniedException.class);

        verify(reviewRepository, never()).save(any());
    }

    // --------------------------------------------------------------------------------------
    // DELETE TESTS
    // --------------------------------------------------------------------------------------

    @Test
    @DisplayName("Delete: Should allow Author to delete")
    void deleteReview_Success_Author() {
        Long reviewId = 1L;
        String username = "me";

        Client author = new Client();
        author.setUsername(username);

        Review review = new Review();
        review.setAuthor(author);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        // Act
        reviewService.deleteReview(reviewId, username, false); // isAdmin = false

        // Assert
        verify(reviewRepository).delete(review);
    }

    @Test
    @DisplayName("Delete: Should allow Admin to delete even if not author")
    void deleteReview_Success_Admin() {
        Long reviewId = 1L;
        String username = "admin";

        Client author = new Client();
        author.setUsername("otherUser");

        Review review = new Review();
        review.setAuthor(author);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        // Act
        reviewService.deleteReview(reviewId, username, true); // isAdmin = true

        // Assert
        verify(reviewRepository).delete(review);
    }

    @Test
    @DisplayName("Delete: Should throw AccessDenied if neither Admin nor Author")
    void deleteReview_Fail_Forbidden() {
        Long reviewId = 1L;
        String username = "stranger";

        Client author = new Client();
        author.setUsername("owner");

        Review review = new Review();
        review.setAuthor(author);

        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(review));

        // Act & Assert
        assertThatThrownBy(() -> reviewService.deleteReview(reviewId, username, false))
                .isInstanceOf(AccessDeniedException.class);

        verify(reviewRepository, never()).delete(any());
    }

    // --------------------------------------------------------------------------------------
    // LIST TESTS
    // --------------------------------------------------------------------------------------

    @Test
    @DisplayName("Get Reviews: Should return mapped list")
    void getReviewsByRealtor() {
        Long realtorId = 5L;
        List<Review> list = List.of(new Review(), new Review());

        when(reviewRepository.findAllByRealtorId(realtorId)).thenReturn(list);
        when(reviewMapper.toDto(any())).thenReturn(new ReviewDto());

        List<ReviewDto> result = reviewService.getReviewsByRealtor(realtorId);

        assertThat(result).hasSize(2);
        verify(reviewRepository).findAllByRealtorId(realtorId);
    }

    // --------------------------------------------------------------------------------------
    // STATISTICS TESTS
    // --------------------------------------------------------------------------------------

    @Test
    @DisplayName("Stats: Should calculate averages correctly")
    void getRealtorStatistics_Success() {
        // Arrange
        Long realtorId = 1L;

        when(userRepository.existsById(realtorId)).thenReturn(true);

        Review r1 = new Review();
        r1.setOverall(5);
        r1.setSpeed(5);
        r1.setCommunication(5);
        r1.setProfessionality(5);
        r1.setFairness(5);

        Review r2 = new Review();
        r2.setOverall(3);
        r2.setSpeed(1);
        r2.setCommunication(3);
        r2.setProfessionality(3);
        r2.setFairness(3);

        // Avg Overall: (5+3)/2 = 4
        // Avg Speed: (5+1)/2 = 3

        when(reviewRepository.findAllByRealtorId(realtorId)).thenReturn(List.of(r1, r2));

        // Act
        ReviewStatisticsDto stats = reviewService.getRealtorStatistics(realtorId);

        // Assert
        assertThat(stats.getTotalReviews()).isEqualTo(2);
        assertThat(stats.getAverageOverall()).isEqualTo(4.0);
        assertThat(stats.getAverageSpeed()).isEqualTo(3.0);
        assertThat(stats.getAverageCommunication()).isEqualTo(4.0);
    }

    @Test
    @DisplayName("Stats: Should return zeros when no reviews exist")
    void getRealtorStatistics_Empty() {
        // Arrange
        Long realtorId = 1L;
        when(userRepository.existsById(realtorId)).thenReturn(true);
        when(reviewRepository.findAllByRealtorId(realtorId)).thenReturn(new ArrayList<>());

        // Act
        ReviewStatisticsDto stats = reviewService.getRealtorStatistics(realtorId);

        // Assert
        assertThat(stats.getTotalReviews()).isZero();
        assertThat(stats.getAverageOverall()).isZero();
    }

    @Test
    @DisplayName("Stats: Should throw exception if Realtor does not exist")
    void getRealtorStatistics_RealtorNotFound() {
        Long realtorId = 99L;
        when(userRepository.existsById(realtorId)).thenReturn(false);

        assertThatThrownBy(() -> reviewService.getRealtorStatistics(realtorId))
                .isInstanceOf(EntityNotFoundException.class);
    }
}