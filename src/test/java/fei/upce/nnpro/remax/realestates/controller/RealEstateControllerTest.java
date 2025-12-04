package fei.upce.nnpro.remax.realestates.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fei.upce.nnpro.remax.realestates.dto.RealEstateDto;
import fei.upce.nnpro.remax.realestates.dto.RealEstateFilterDto;
import fei.upce.nnpro.remax.realestates.dto.RealEstateMapper;
import fei.upce.nnpro.remax.realestates.entity.Apartment;
import fei.upce.nnpro.remax.realestates.service.RealEstateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RealEstateControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RealEstateService realEstateService;

    @Mock
    private RealEstateMapper realEstateMapper;

    @InjectMocks
    private RealEstateController realEstateController;

    @BeforeEach
    void setUp() {
        // No-op validator to skip @Valid checks for complex DTOs in unit tests
        Validator noopValidator = new Validator() {
            @Override
            public boolean supports(Class<?> clazz) { return false; }

            @Override
            public void validate(Object target, Errors errors) { /* no-op */ }
        };

        mockMvc = MockMvcBuilders.standaloneSetup(realEstateController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .setValidator(noopValidator)
                .build();
    }

    @Test
    void createRealEstate_returnsCreated() throws Exception {
        RealEstateDto dto = new RealEstateDto();
        dto.setName("n");

        Apartment created = new Apartment();
        Mockito.when(realEstateService.createRealEstate(any(RealEstateDto.class))).thenReturn(created);
        Mockito.when(realEstateMapper.toDto(created)).thenReturn(dto);

        mockMvc.perform(post("/api/real-estates")
                        .principal(new TestingAuthenticationToken("realtor", "p", "ROLE_REALTOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("n"));
    }

    @Test
    void getRealEstate_returnsOk() throws Exception {
        Apartment e = new Apartment();
        RealEstateDto dto = new RealEstateDto();
        dto.setName("g");
        Mockito.when(realEstateService.getRealEstate(1L)).thenReturn(e);
        Mockito.when(realEstateMapper.toDto(e)).thenReturn(dto);

        mockMvc.perform(get("/api/real-estates/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("g"));
    }

    @Test
    @Disabled
    void searchRealEstates_returnsPage() throws Exception {
        Apartment e = new Apartment();
        e.setName("estate");
        RealEstateDto dto = new RealEstateDto();
        dto.setName("p");
        Mockito.when(realEstateService.searchRealEstates(any(RealEstateFilterDto.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(e)));
        Mockito.when(realEstateMapper.toDto(e)).thenReturn(dto);

        mockMvc.perform(get("/api/real-estates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("p"));
    }
}
