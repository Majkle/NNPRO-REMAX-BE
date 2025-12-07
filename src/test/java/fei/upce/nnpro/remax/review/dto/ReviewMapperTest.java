package fei.upce.nnpro.remax.review.dto;

import fei.upce.nnpro.remax.profile.entity.Client;
import fei.upce.nnpro.remax.profile.entity.Realtor;
import fei.upce.nnpro.remax.review.entity.Review;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ReviewMapperTest {

    @InjectMocks
    private ReviewMapper reviewMapper;

    @Test
    void toDto_WithFullEntity_ShouldMapAllFields() {
        Review entity = new Review();
        entity.setId(1L);
        entity.setOverall(5);
        entity.setSpeed(4);
        entity.setCommunication(5);
        entity.setProfessionality(4);
        entity.setFairness(5);
        entity.setText("Great service!");
        entity.setClientDisplayName("John Doe");

        Realtor realtor = new Realtor();
        realtor.setId(10L);
        entity.setRealtor(realtor);

        Client author = new Client();
        author.setId(20L);
        entity.setAuthor(author);

        ReviewDto dto = reviewMapper.toDto(entity);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getOverall()).isEqualTo(5);
        assertThat(dto.getSpeed()).isEqualTo(4);
        assertThat(dto.getCommunication()).isEqualTo(5);
        assertThat(dto.getProfessionality()).isEqualTo(4);
        assertThat(dto.getFairness()).isEqualTo(5);
        assertThat(dto.getText()).isEqualTo("Great service!");
        assertThat(dto.getClientDisplayName()).isEqualTo("John Doe");
        assertThat(dto.getRealtorId()).isEqualTo(10L);
        assertThat(dto.getAuthorClientId()).isEqualTo(20L);
    }

    @Test
    void toDto_WithNullEntity_ShouldReturnNull() {
        ReviewDto dto = reviewMapper.toDto(null);
        assertThat(dto).isNull();
    }

    @Test
    void toDto_WithoutRealtor_ShouldMapWithoutRealtorId() {
        Review entity = new Review();
        entity.setId(1L);
        entity.setOverall(3);
        entity.setSpeed(3);
        entity.setCommunication(3);
        entity.setProfessionality(3);
        entity.setFairness(3);
        entity.setText("Good");
        entity.setClientDisplayName("Jane Doe");

        Client author = new Client();
        author.setId(20L);
        entity.setAuthor(author);

        ReviewDto dto = reviewMapper.toDto(entity);

        assertThat(dto).isNotNull();
        assertThat(dto.getRealtorId()).isNull();
        assertThat(dto.getAuthorClientId()).isEqualTo(20L);
    }

    @Test
    void toDto_WithoutAuthor_ShouldMapWithoutAuthorId() {
        Review entity = new Review();
        entity.setId(1L);
        entity.setOverall(3);
        entity.setSpeed(3);
        entity.setCommunication(3);
        entity.setProfessionality(3);
        entity.setFairness(3);
        entity.setText("Good");
        entity.setClientDisplayName("Jane Doe");

        Realtor realtor = new Realtor();
        realtor.setId(10L);
        entity.setRealtor(realtor);

        ReviewDto dto = reviewMapper.toDto(entity);

        assertThat(dto).isNotNull();
        assertThat(dto.getRealtorId()).isEqualTo(10L);
        assertThat(dto.getAuthorClientId()).isNull();
    }

    @Test
    void toEntity_WithFullDto_ShouldMapFields() {
        ReviewDto dto = new ReviewDto();
        dto.setId(1L);
        dto.setOverall(5);
        dto.setSpeed(4);
        dto.setCommunication(5);
        dto.setProfessionality(4);
        dto.setFairness(5);
        dto.setText("Great service!");
        dto.setRealtorId(10L);
        dto.setAuthorClientId(20L);
        dto.setClientDisplayName("John Doe");

        Review entity = reviewMapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getOverall()).isEqualTo(5);
        assertThat(entity.getSpeed()).isEqualTo(4);
        assertThat(entity.getCommunication()).isEqualTo(5);
        assertThat(entity.getProfessionality()).isEqualTo(4);
        assertThat(entity.getFairness()).isEqualTo(5);
        assertThat(entity.getText()).isEqualTo("Great service!");
    }

    @Test
    void toEntity_WithNullDto_ShouldReturnNull() {
        Review entity = reviewMapper.toEntity(null);
        assertThat(entity).isNull();
    }

    @Test
    void toEntity_WithMinimalDto_ShouldMapRequiredFields() {
        ReviewDto dto = new ReviewDto();
        dto.setOverall(3);
        dto.setSpeed(3);
        dto.setCommunication(3);
        dto.setProfessionality(3);
        dto.setFairness(3);
        dto.setText("Okay");

        Review entity = reviewMapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getOverall()).isEqualTo(3);
        assertThat(entity.getSpeed()).isEqualTo(3);
        assertThat(entity.getCommunication()).isEqualTo(3);
        assertThat(entity.getProfessionality()).isEqualTo(3);
        assertThat(entity.getFairness()).isEqualTo(3);
        assertThat(entity.getText()).isEqualTo("Okay");
    }
}
