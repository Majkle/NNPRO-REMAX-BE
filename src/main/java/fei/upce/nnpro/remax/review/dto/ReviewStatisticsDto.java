package fei.upce.nnpro.remax.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Aggregated review statistics for a specific realtor")
public class ReviewStatisticsDto {
    @Schema(description = "ID of the realtor", example = "10")
    private Long realtorId;

    @Schema(description = "Total number of reviews received", example = "42")
    private long totalReviews;

    @Schema(description = "Average overall rating (0.0 - 5.0)", example = "4.5")
    private double averageOverall;

    @Schema(description = "Average speed rating (0.0 - 5.0)", example = "4.8")
    private double averageSpeed;

    @Schema(description = "Average communication rating (0.0 - 5.0)", example = "4.2")
    private double averageCommunication;

    @Schema(description = "Average professionality rating (0.0 - 5.0)", example = "4.9")
    private double averageProfessionality;

    @Schema(description = "Average fairness rating (0.0 - 5.0)", example = "4.6")
    private double averageFairness;
}
