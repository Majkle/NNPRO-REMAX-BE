package fei.upce.nnprop.remax.images.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "Response object representing an uploaded image")
public class ImageDto {
    @Schema(description = "Unique ID of the image", example = "10")
    private Long id;

    @Schema(description = "Original filename", example = "living_room.jpg")
    private String filename;

    @Schema(description = "MIME type of the file", example = "image/jpeg")
    private String contentType;

    @Schema(description = "Size of the file in bytes", example = "1048576")
    private long size;

    @Schema(description = "API URL to download/view the image", example = "http://localhost:8080/api/images/10")
    private String downloadUrl;
}