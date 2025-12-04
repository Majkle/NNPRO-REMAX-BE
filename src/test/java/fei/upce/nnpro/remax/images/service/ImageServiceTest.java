package fei.upce.nnpro.remax.images.service;

import fei.upce.nnpro.remax.images.dto.ImageDto;
import fei.upce.nnpro.remax.images.entity.Image;
import fei.upce.nnpro.remax.images.repository.ImageRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ImageServiceTest {

    @Mock
    private ImageRepository imageRepository;

    private ImageService sut;

    @BeforeEach
    void setUp() {
        sut = new ImageService(imageRepository);
    }

    @Test
    void uploadImage_shouldSaveAndReturnDto() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("pic.png");
        when(file.getContentType()).thenReturn("image/png");
        when(file.getBytes()).thenReturn(new byte[]{1,2,3});
        Image saved = new Image();
        saved.setId(11L);
        saved.setFilename("pic.png");
        saved.setContentType("image/png");
        saved.setData(new byte[]{1,2,3});
        when(imageRepository.save(org.mockito.Mockito.any())).thenReturn(saved);

        // Mock static ServletUriComponentsBuilder
        try (MockedStatic<ServletUriComponentsBuilder> mocked = mockStatic(ServletUriComponentsBuilder.class)) {
            ServletUriComponentsBuilder builder = mock(ServletUriComponentsBuilder.class);
            when(ServletUriComponentsBuilder.fromCurrentContextPath()).thenReturn(builder);
            when(builder.path(anyString())).thenReturn(builder);
            when(builder.toUriString()).thenReturn("/api/images/11");

            ImageDto dto = sut.uploadImage(file);
            assertThat(dto.getId()).isEqualTo(11L);
            assertThat(dto.getFilename()).isEqualTo("pic.png");
            assertThat(dto.getSize()).isEqualTo(3);
        }

        verify(imageRepository).save(org.mockito.Mockito.any());
    }

    @Test
    void uploadImage_emptyFile_shouldThrow() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);
        when(file.getOriginalFilename()).thenReturn("empty.png");

        assertThatThrownBy(() -> sut.uploadImage(file)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot upload empty file");
    }

    @Test
    void getImageEntity_shouldReturnOrThrow() {
        Image i = new Image();
        i.setId(22L);
        when(imageRepository.findById(22L)).thenReturn(Optional.of(i));
        Image got = sut.getImageEntity(22L);
        assertThat(got).isSameAs(i);

        when(imageRepository.findById(33L)).thenReturn(Optional.empty());
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> sut.getImageEntity(33L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void deleteImage_shouldDeleteOrThrow() {
        when(imageRepository.existsById(5L)).thenReturn(true);
        sut.deleteImage(5L);
        verify(imageRepository).deleteById(5L);

        when(imageRepository.existsById(6L)).thenReturn(false);
        assertThatThrownBy(() -> sut.deleteImage(6L)).isInstanceOf(EntityNotFoundException.class);
    }
}
