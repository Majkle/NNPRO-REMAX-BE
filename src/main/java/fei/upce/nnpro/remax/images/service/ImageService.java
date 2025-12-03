package fei.upce.nnpro.remax.images.service;

import fei.upce.nnpro.remax.images.dto.ImageDto;
import fei.upce.nnpro.remax.images.entity.Image;
import fei.upce.nnpro.remax.images.repository.ImageRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private static final Logger log = LoggerFactory.getLogger(ImageService.class);

    @Transactional
    public ImageDto uploadImage(MultipartFile file) throws IOException {
        log.info("Uploading image filename={} size={}", file.getOriginalFilename(), file.getSize());
        if (file.isEmpty()) {
            log.warn("Attempted to upload empty file: {}", file.getOriginalFilename());
            throw new IllegalArgumentException("Cannot upload empty file");
        }

        Image image = new Image();
        image.setFilename(file.getOriginalFilename());
        image.setContentType(file.getContentType());
        image.setData(file.getBytes());

        Image savedImage = imageRepository.save(image);
        log.info("Uploaded image id={} filename={}", savedImage.getId(), savedImage.getFilename());

        return mapToDto(savedImage);
    }

    @Transactional(readOnly = true)
    public Image getImageEntity(Long id) {
        log.info("Fetching image entity id={}", id);
        Image found = imageRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Image not found id={}", id);
                    return new EntityNotFoundException("Image not found with id: " + id);
                });
        log.debug("Fetched image id={} filename={}", found.getId(), found.getFilename());
        return found;
    }

    @Transactional
    public void deleteImage(Long id) {
        log.info("Deleting image id={}", id);
        if (!imageRepository.existsById(id)) {
            log.warn("Attempted to delete non-existing image id={}", id);
            throw new EntityNotFoundException("Image not found with id: " + id);
        }
        imageRepository.deleteById(id);
        log.info("Deleted image id={}", id);
    }

    private ImageDto mapToDto(Image image) {
        String downloadUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/images/")
                .path(String.valueOf(image.getId()))
                .toUriString();

        ImageDto dto = ImageDto.builder()
                .id(image.getId())
                .filename(image.getFilename())
                .contentType(image.getContentType())
                .size(image.getData() != null ? image.getData().length : 0)
                .downloadUrl(downloadUrl)
                .build();

        log.debug("Mapped image id={} to ImageDto downloadUrl={}", image.getId(), downloadUrl);
        return dto;
    }
}