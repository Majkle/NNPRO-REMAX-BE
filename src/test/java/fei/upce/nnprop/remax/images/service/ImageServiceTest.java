package fei.upce.nnprop.remax.images.service;

import fei.upce.nnprop.remax.images.dto.ImageDto;
import fei.upce.nnprop.remax.images.repository.ImageRepository;
import fei.upce.nnprop.remax.model.image.Image;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private ImageRepository imageRepository;

    @InjectMocks
    private ImageService imageService;

    @Test
    void uploadImage_Success() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.jpg", "image/jpeg", "data".getBytes());

        // Mock RequestContext for URI builder
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        when(imageRepository.save(any(Image.class))).thenAnswer(i -> {
            Image img = i.getArgument(0);
            img.setId(1L);
            return img;
        });

        // Act
        ImageDto result = imageService.uploadImage(file);

        // Assert
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFilename()).isEqualTo("test.jpg");
        assertThat(result.getContentType()).isEqualTo("image/jpeg");
        verify(imageRepository).save(any(Image.class));
    }

    @Test
    void getImage_Success() {
        Image img = new Image();
        img.setId(1L);
        img.setData(new byte[]{1, 2, 3});
        when(imageRepository.findById(1L)).thenReturn(Optional.of(img));

        Image result = imageService.getImageEntity(1L);
        assertThat(result.getData()).hasSize(3);
    }

    @Test
    void deleteImage_Success() {
        when(imageRepository.existsById(1L)).thenReturn(true);
        imageService.deleteImage(1L);
        verify(imageRepository).deleteById(1L);
    }

    @Test
    void deleteImage_NotFound() {
        when(imageRepository.existsById(1L)).thenReturn(false);
        assertThatThrownBy(() -> imageService.deleteImage(1L))
                .isInstanceOf(EntityNotFoundException.class);
    }
}