package fei.upce.nnpro.remax.review.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fei.upce.nnpro.remax.review.dto.ReviewDto;
import fei.upce.nnpro.remax.review.dto.ReviewStatisticsDto;
import fei.upce.nnpro.remax.review.service.ReviewService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ReviewControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ReviewService reviewService;

    @InjectMocks
    private ReviewController reviewController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reviewController)
                .build();
    }

    @Test
    void createReview_returnsCreated() throws Exception {
        ReviewDto req = new ReviewDto();
        req.setText("ok");
        req.setOverall(4);
        req.setSpeed(4);
        req.setCommunication(4);
        req.setProfessionality(4);
        req.setFairness(4);
        req.setRealtorId(1L);

        ReviewDto created = new ReviewDto();
        created.setText("ok");

        Mockito.when(reviewService.createReview(any(ReviewDto.class), eq("user"))).thenReturn(created);

        mockMvc.perform(post("/api/reviews")
                        .principal(new TestingAuthenticationToken("user", "p", "ROLE_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.text").value("ok"));
    }

    @Test
    void deleteReview_callsService_andReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/reviews/1")
                        .principal(new TestingAuthenticationToken("user", "p", "ROLE_USER")))
                .andExpect(status().isNoContent());

        Mockito.verify(reviewService).deleteReview(eq(1L), eq("user"), any(Boolean.class));
    }

    @Test
    void getReviewsByRealtor_returnsList() throws Exception {
        ReviewDto r = new ReviewDto();
        r.setText("c");
        r.setOverall(5);
        r.setSpeed(5);
        r.setCommunication(5);
        r.setProfessionality(5);
        r.setFairness(5);
        r.setRealtorId(2L);
        Mockito.when(reviewService.getReviewsByRealtor(2L)).thenReturn(List.of(r));

        mockMvc.perform(get("/api/reviews/realtor/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].text").value("c"));
    }

    @Test
    void getRealtorStatistics_returnsDto() throws Exception {
        ReviewStatisticsDto stats = Mockito.mock(ReviewStatisticsDto.class);
        Mockito.when(reviewService.getRealtorStatistics(3L)).thenReturn(stats);

        mockMvc.perform(get("/api/reviews/stats/3"))
                .andExpect(status().isOk());
    }
}
