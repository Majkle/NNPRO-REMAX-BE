package fei.upce.nnprop.remax.review.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewStatisticsDto {
    private Long realtorId;
    private long totalReviews;
    private double averageOverall;
    private double averageSpeed;
    private double averageCommunication;
    private double averageProfessionality;
    private double averageFairness;
}
