package fei.upce.nnpro.remax.images.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fei.upce.nnpro.remax.images.dto.ImageDto;
import fei.upce.nnpro.remax.images.entity.Image;
import fei.upce.nnpro.remax.images.service.ImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ImageControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ImageService imageService;

    @InjectMocks
    private ImageController imageController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(imageController)
                .build();
    }

    @Test
    void uploadImage_returnsCreated() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "name.jpg", "image/jpeg", "data".getBytes());
        ImageDto dto = ImageDto.builder().filename("name.jpg").build();
        Mockito.when(imageService.uploadImage(any())).thenReturn(dto);

        mockMvc.perform(multipart("/api/images").file(file)
                        .with(user("realtor").roles("REALTOR"))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.filename").value("name.jpg"));
    }

    @Test
    void getImage_returnsByteArray_andHeaders() throws Exception {
        Image img = new Image();
        img.setFilename("f.jpg");
        img.setContentType("image/jpeg");
        img.setData("bytes".getBytes());
        Mockito.when(imageService.getImageEntity(1L)).thenReturn(img);

        mockMvc.perform(get("/api/images/1"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("f.jpg")))
                .andExpect(content().contentType("image/jpeg"));
    }

    @Test
    void deleteImage_requiresRoleAndReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/images/1").principal(new TestingAuthenticationToken("realtor", "p", "ROLE_REALTOR")))
                .andExpect(status().isNoContent());

        Mockito.verify(imageService).deleteImage(1L);
    }
}
