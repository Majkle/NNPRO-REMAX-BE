package fei.upce.nnpro.remax.images.controller;

import fei.upce.nnpro.remax.images.dto.ImageDto;
import fei.upce.nnpro.remax.images.entity.Image;
import fei.upce.nnpro.remax.images.service.ImageService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.io.IOException;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Tag(name = "Images", description = "Endpoints for uploading, retrieving, and deleting images")
public class ImageController {

    private final ImageService imageService;

    /**
     * Upload an image.
     * Use form-data with key 'file'.
     */
    @Operation(
            summary = "Upload an image",
            description = "Uploads an image file to the database. Restricted to REALTOR role.",
            security = @SecurityRequirement(name = "bearerAuth") // Links to the security scheme in OpenApiConfig
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Image uploaded successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ImageDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "Invalid file (empty or malformed)"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have REALTOR role"),
            @ApiResponse(responseCode = "500", description = "Server error during upload")
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('REALTOR')")
    public ResponseEntity<ImageDto> uploadImage(
            @Parameter(
                description = "The image file to upload",
                required = true,
                content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE)
            )
            @RequestParam("file") MultipartFile file) {
        try {
            ImageDto imageDto = imageService.uploadImage(file);
            return new ResponseEntity<>(imageDto, HttpStatus.CREATED);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Retrieve the raw image data.
     * This endpoint is used in HTML <img src="/api/images/{id}"> tags.
     */
    @Operation(
            summary = "Get image content",
            description = "Returns the raw binary data of the image. Suitable for HTML <img src='...'> tags."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Image retrieved successfully",
                    content = @Content(mediaType = "image/jpeg", schema = @Schema(type = "string", format = "binary"))
            ),
            @ApiResponse(responseCode = "404", description = "Image not found for the given ID")
    })
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(
            @Parameter(description = "ID of the image to retrieve", example = "1")
            @PathVariable Long id) {
        Image image = imageService.getImageEntity(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + image.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .body(image.getData());
    }

    /**
     * Delete an image by ID.
     */
    @Operation(
            summary = "Delete an image",
            description = "Permanently removes an image from the database. Restricted to REALTOR role.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Image deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid JWT"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have REALTOR role"),
            @ApiResponse(responseCode = "404", description = "Image not found")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('REALTOR')")
    public ResponseEntity<Void> deleteImage(
            @Parameter(description = "ID of the image to delete", example = "1")
            @PathVariable Long id) {
        imageService.deleteImage(id);
        return ResponseEntity.noContent().build();
    }
}
