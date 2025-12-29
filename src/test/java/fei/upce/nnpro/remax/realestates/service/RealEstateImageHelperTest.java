package fei.upce.nnpro.remax.realestates.service;

import fei.upce.nnpro.remax.images.entity.Image;
import fei.upce.nnpro.remax.images.repository.ImageRepository;
import fei.upce.nnpro.remax.realestates.entity.Apartment;
import fei.upce.nnpro.remax.realestates.entity.RealEstate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RealEstateImageHelperTest {

    @Mock
    private ImageRepository imageRepository;

    @InjectMocks
    private RealEstateImageHelper helper;

    @Test
    @DisplayName("Handle Images: Should add new images and set relationship")
    void handleImages_AddImages() {
        RealEstate estate = new Apartment();
        estate.setId(1L);
        estate.setImages(new ArrayList<>()); // Empty currently

        List<Long> newIds = List.of(10L, 11L);
        Image img1 = new Image(); img1.setId(10L);
        Image img2 = new Image(); img2.setId(11L);

        when(imageRepository.findAllById(newIds)).thenReturn(List.of(img1, img2));

        helper.handleImages(estate, newIds);

        // Verify images were added to entity
        assertThat(estate.getImages()).hasSize(2);
        assertThat(estate.getImages()).contains(img1, img2);

        // Verify back-reference was set
        assertThat(img1.getRealEstate()).isEqualTo(estate);
        assertThat(img2.getRealEstate()).isEqualTo(estate);
    }

    @Test
    @DisplayName("Handle Images: Should clear images if list is empty")
    void handleImages_ClearImages() {
        RealEstate estate = new Apartment();
        Image existing = new Image();
        estate.setImages(new ArrayList<>(List.of(existing)));

        helper.handleImages(estate, Collections.emptyList());

        assertThat(estate.getImages()).isEmpty();
        verifyNoInteractions(imageRepository);
    }

    @Test
    @DisplayName("Handle Images: Should do nothing if list is null")
    void handleImages_NullList() {
        RealEstate estate = new Apartment();
        Image existing = new Image();
        estate.setImages(new ArrayList<>(List.of(existing)));

        helper.handleImages(estate, null);

        assertThat(estate.getImages()).hasSize(1); // Unchanged
    }

    @Test
    @DisplayName("Handle Images: Should initialize list if entity list is null")
    void handleImages_NullEntityList() {
        RealEstate estate = new Apartment();
        estate.setImages(null);

        List<Long> newIds = List.of(5L);
        Image img = new Image();
        when(imageRepository.findAllById(newIds)).thenReturn(List.of(img));

        helper.handleImages(estate, newIds);

        assertThat(estate.getImages()).isNotNull();
        assertThat(estate.getImages()).hasSize(1);
    }
}
