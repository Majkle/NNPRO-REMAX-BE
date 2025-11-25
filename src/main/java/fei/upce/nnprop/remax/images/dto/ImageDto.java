package fei.upce.nnprop.remax.images.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImageDto {
    private Long id;
    private String filename;
    private String contentType;
    private long size;
    private String downloadUrl; // Helper URL for the frontend
}