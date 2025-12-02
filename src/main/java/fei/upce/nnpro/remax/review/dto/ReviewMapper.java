package fei.upce.nnpro.remax.review.dto;

import fei.upce.nnpro.remax.review.entity.Review;
import org.springframework.stereotype.Component;

@Component
public class ReviewMapper {

    public ReviewDto toDto(Review entity) {
        if (entity == null) return null;

        ReviewDto dto = new ReviewDto();
        dto.setId(entity.getId());
        dto.setOverall(entity.getOverall());
        dto.setSpeed(entity.getSpeed());
        dto.setCommunication(entity.getCommunication());
        dto.setProfessionality(entity.getProfessionality());
        dto.setFairness(entity.getFairness());
        dto.setText(entity.getText());
        dto.setClientDisplayName(entity.getClientDisplayName());

        if (entity.getRealtor() != null) {
            dto.setRealtorId(entity.getRealtor().getId());
        }
        if (entity.getAuthor() != null) {
            dto.setAuthorClientId(entity.getAuthor().getId());
        }

        return dto;
    }

    public Review toEntity(ReviewDto dto) {
        if (dto == null) return null;

        Review review = new Review();
        // ID is usually not set manually on creation, handled by service/DB
        review.setOverall(dto.getOverall());
        review.setSpeed(dto.getSpeed());
        review.setCommunication(dto.getCommunication());
        review.setProfessionality(dto.getProfessionality());
        review.setFairness(dto.getFairness());
        review.setText(dto.getText());

        // Note: Relations (Realtor, Author) are resolved in the Service
        return review;
    }
}