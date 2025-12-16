package fei.upce.nnpro.remax.review.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Response object representing a simplified realtor")
public class RealtorSimplifiedDto {
    @Schema(description = "Unique identifier of the realtor", example = "10")
    private Long id;

    @Schema(description = "Degree", example = "Ing.")
    private String degree;

    @Schema(description = "First name", example = "Jan")
    private String firstName;

    @Schema(description = "First name", example = "Novák")
    private String lastName;
}
