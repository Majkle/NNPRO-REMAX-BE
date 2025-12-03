package fei.upce.nnprop.remax.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Review data")
public class ReviewDto {
    @Schema(description = "Review ID", example = "1")
    private Long id;

    @Min(0) @Max(5)
    @Schema(description = "Overall rating (0-5)", example = "5")
    private int overall;

    @Min(0) @Max(5)
    @Schema(description = "Speed rating (0-5)", example = "4")
    private int speed;

    @Min(0) @Max(5)
    @Schema(description = "Communication rating (0-5)", example = "5")
    private int communication;

    @Min(0) @Max(5)
    @Schema(description = "Professionality rating (0-5)", example = "5")
    private int professionality;

    @Min(0) @Max(5)
    @Schema(description = "Fairness rating (0-5)", example = "4")
    private int fairness;

    @NotBlank
    @Size(max = 500)
    @Schema(description = "Textual comment", example = "Excellent service, very fast.")
    private String text;

    @NotNull
    @Schema(description = "ID of the realtor being reviewed", example = "2")
    private Long realtorId;

    @Schema(description = "[Output Only] ID of the author", accessMode = Schema.AccessMode.READ_ONLY)
    private Long authorClientId;

    @Schema(description = "[Output Only] Display name of the author", example = "John Doe", accessMode = Schema.AccessMode.READ_ONLY)
    private String clientDisplayName;
}
