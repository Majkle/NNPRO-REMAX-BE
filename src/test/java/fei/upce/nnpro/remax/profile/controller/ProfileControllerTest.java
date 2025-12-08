package fei.upce.nnpro.remax.profile.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fei.upce.nnpro.remax.profile.dto.ProfileUpdateRequest;
import fei.upce.nnpro.remax.profile.entity.Admin;
import fei.upce.nnpro.remax.profile.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
class ProfileControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ProfileService profileService;

    @InjectMocks
    private ProfileController profileController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(profileController)
                .build();
    }

    @Test
    @Disabled
    void getProfile_authenticated_returnsUser() throws Exception {
        Admin u = new Admin();
        u.setUsername("bob");
        Mockito.when(profileService.getProfile("bob")).thenReturn(Optional.of(u));

        mockMvc.perform(get("/api/profile").principal(new TestingAuthenticationToken("bob", "p", "ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("bob"));
    }

    @Test
    @Disabled
    void updateProfile_authenticated_returnsUpdated() throws Exception {
        ProfileUpdateRequest req = new ProfileUpdateRequest();
        req.setFirstName("First");
        req.setLastName("Last");
        req.setPhoneNumber("123");
        req.setBirthDate("2000-01-01");
        req.setStreet("s");
        req.setCity("c");
        req.setPostalCode("pc");
        req.setCountry("ct");
        req.setRegion("r");

        Admin updated = new Admin();
        updated.setUsername("bob");
        Mockito.when(profileService.updateProfile(eq("bob"), any(ProfileUpdateRequest.class))).thenReturn(updated);

        mockMvc.perform(patch("/api/profile")
                        .with(user("bob").authorities(new SimpleGrantedAuthority("ROLE_ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("bob"));
    }

    @Test
    @Disabled
    void deleteProfile_authenticated_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/profile").principal(new TestingAuthenticationToken("bob", "p", "ROLE_USER")))
                .andExpect(status().isNoContent());

        Mockito.verify(profileService).deleteProfile("bob");
    }
}
