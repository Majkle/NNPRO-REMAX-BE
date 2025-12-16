package fei.upce.nnpro.remax.review.service;

import fei.upce.nnpro.remax.profile.entity.Client;
import fei.upce.nnpro.remax.profile.entity.PersonalInformation;
import fei.upce.nnpro.remax.profile.entity.Realtor;
import fei.upce.nnpro.remax.profile.entity.RemaxUser;
import fei.upce.nnpro.remax.profile.repository.RemaxUserRepository;
import fei.upce.nnpro.remax.review.dto.RealtorSimplifiedDto;
import fei.upce.nnpro.remax.review.dto.ReviewDto;
import fei.upce.nnpro.remax.review.dto.ReviewMapper;
import fei.upce.nnpro.remax.review.dto.ReviewStatisticsDto;
import fei.upce.nnpro.remax.review.entity.Review;
import fei.upce.nnpro.remax.review.repository.ReviewRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final RemaxUserRepository userRepository;
    private final ReviewMapper reviewMapper;

    @Transactional
    public ReviewDto createReview(ReviewDto dto, String authorUsername) {
        RemaxUser user = userRepository.findByUsername(authorUsername)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        if (!(user instanceof Client author)) {
            throw new IllegalArgumentException("Only Clients can write reviews.");
        }

        Realtor realtor = (Realtor) userRepository.findById(dto.getRealtorId())
                .filter(u -> u instanceof Realtor)
                .orElseThrow(() -> new EntityNotFoundException("Realtor not found with ID " + dto.getRealtorId()));

        Review review = reviewMapper.toEntity(dto);
        review.setAuthor(author);
        review.setRealtor(realtor);

        // Set display name from Personal Information
        String displayName = author.getPersonalInformation() != null
                ? author.getPersonalInformation().getFirstName() + " " + author.getPersonalInformation().getLastName()
                : author.getUsername();
        review.setClientDisplayName(displayName);

        Review saved = reviewRepository.save(review);
        return reviewMapper.toDto(saved);
    }

    @Transactional
    public ReviewDto updateReview(Long reviewId, ReviewDto dto, String username) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found"));

        // Check ownership
        if (!review.getAuthor().getUsername().equals(username)) {
            throw new AccessDeniedException("You are not the author of this review.");
        }

        review.setOverall(dto.getOverall());
        review.setSpeed(dto.getSpeed());
        review.setCommunication(dto.getCommunication());
        review.setProfessionality(dto.getProfessionality());
        review.setFairness(dto.getFairness());
        review.setText(dto.getText());

        return reviewMapper.toDto(reviewRepository.save(review));
    }

    @Transactional
    public void deleteReview(Long reviewId, String username, boolean isAdmin) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new EntityNotFoundException("Review not found"));

        if (!isAdmin && !review.getAuthor().getUsername().equals(username)) {
            throw new AccessDeniedException("You do not have permission to delete this review.");
        }

        reviewRepository.delete(review);
    }

    @Transactional(readOnly = true)
    public List<RealtorSimplifiedDto> getAllRealtors() {
        return userRepository.findAllRealtors().stream()
                .map((r) -> {
                    PersonalInformation pi = r.getPersonalInformation();
                    RealtorSimplifiedDto rs = new RealtorSimplifiedDto();
                    rs.setId(r.getId());
                    rs.setDegree(pi.getDegree());
                    rs.setFirstName(pi.getFirstName());
                    rs.setLastName(pi.getLastName());
                    return rs;
                }).toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> getReviewsByRealtor(Long realtorId) {
        return reviewRepository.findAllByRealtorId(realtorId).stream()
                .map(reviewMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReviewDto> listAllReviews() {
        return reviewRepository.findAll().stream()
                .map(reviewMapper::toDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReviewStatisticsDto getRealtorStatistics(Long realtorId) {
        // Verify realtor exists
        if (!userRepository.existsById(realtorId)) {
            throw new EntityNotFoundException("Realtor not found");
        }

        List<Review> reviews = reviewRepository.findAllByRealtorId(realtorId);

        long count = reviews.size();
        if (count == 0) {
            return ReviewStatisticsDto.builder()
                    .realtorId(realtorId)
                    .totalReviews(0)
                    .build(); // other doubles default to 0.0
        }

        // Calculate averages
        double avgOverall = reviews.stream().mapToInt(Review::getOverall).average().orElse(0.0);
        double avgSpeed = reviews.stream().mapToInt(Review::getSpeed).average().orElse(0.0);
        double avgComm = reviews.stream().mapToInt(Review::getCommunication).average().orElse(0.0);
        double avgProf = reviews.stream().mapToInt(Review::getProfessionality).average().orElse(0.0);
        double avgFair = reviews.stream().mapToInt(Review::getFairness).average().orElse(0.0);

        return ReviewStatisticsDto.builder()
                .realtorId(realtorId)
                .totalReviews(count)
                .averageOverall(avgOverall)
                .averageSpeed(avgSpeed)
                .averageCommunication(avgComm)
                .averageProfessionality(avgProf)
                .averageFairness(avgFair)
                .build();
    }
}
