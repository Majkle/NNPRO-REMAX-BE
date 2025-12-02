package fei.upce.nnpro.remax.review.repository;

import fei.upce.nnpro.remax.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findAllByRealtorId(Long realtorId);
}