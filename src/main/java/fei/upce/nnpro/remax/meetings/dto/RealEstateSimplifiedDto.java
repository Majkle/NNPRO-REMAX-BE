package fei.upce.nnpro.remax.meetings.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Response object representing a simplified real estate")
public class RealEstateSimplifiedDto {
    @Schema(description = "Unique identifier of the real estate", example = "10")
    private Long id;

    @Schema(description = "Title", example = "Malý útulný byt")
    private String title;
}
