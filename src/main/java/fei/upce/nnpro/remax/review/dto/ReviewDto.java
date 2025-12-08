package fei.upce.nnpro.remax.review.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewDto {

    private Long id;

    @Min(0) @Max(5)
    private int overall;

    @Min(0) @Max(5)
    private int speed;

    @Min(0) @Max(5)
    private int communication;

    @Min(0) @Max(5)
    private int professionality;

    @Min(0) @Max(5)
    private int fairness;

    @NotBlank
    @Size(max = 500)
    private String text;

    // ID of the realtor being reviewed
    @NotNull
    private Long realtorId;

    // Output only: Information about the author
    private Long authorClientId;
    private String clientDisplayName;
}
