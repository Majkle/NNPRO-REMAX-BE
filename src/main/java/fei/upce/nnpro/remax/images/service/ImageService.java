package fei.upce.nnpro.remax.images.service;

import fei.upce.nnpro.remax.images.dto.ImageDto;
import fei.upce.nnpro.remax.images.entity.Image;
import fei.upce.nnpro.remax.images.repository.ImageRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;

    @Transactional
    public ImageDto uploadImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot upload empty file");
        }

        Image image = new Image();
        image.setFilename(file.getOriginalFilename());
        image.setContentType(file.getContentType());
        image.setData(file.getBytes());

        Image savedImage = imageRepository.save(image);

        return mapToDto(savedImage);
    }

    @Transactional(readOnly = true)
    public Image getImageEntity(Long id) {
        return imageRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Image not found with id: " + id));
    }

    @Transactional
    public void deleteImage(Long id) {
        if (!imageRepository.existsById(id)) {
            throw new EntityNotFoundException("Image not found with id: " + id);
        }
        imageRepository.deleteById(id);
    }

    private ImageDto mapToDto(Image image) {
        String downloadUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/api/images/")
                .path(String.valueOf(image.getId()))
                .toUriString();

        return ImageDto.builder()
                .id(image.getId())
                .filename(image.getFilename())
                .contentType(image.getContentType())
                .size(image.getData() != null ? image.getData().length : 0)
                .downloadUrl(downloadUrl)
                .build();
    }
}