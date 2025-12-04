package fei.upce.nnpro.remax.security.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fei.upce.nnpro.remax.profile.entity.RemaxUser;
import fei.upce.nnpro.remax.profile.repository.RemaxUserRepository;
import fei.upce.nnpro.remax.security.auth.request.AuthRequest;
import fei.upce.nnpro.remax.security.auth.request.PasswordResetConfirmRequest;
import fei.upce.nnpro.remax.security.auth.request.PasswordResetRequest;
import fei.upce.nnpro.remax.security.auth.request.RegisterRequest;
import fei.upce.nnpro.remax.security.auth.response.AuthResponse;
import fei.upce.nnpro.remax.security.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.ZonedDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    @Mock
    private RemaxUserRepository userRepository;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void login_happyPath_returnsAuthResponse() throws Exception {
        AuthRequest req = new AuthRequest();
        req.setUsername("john");
        req.setPassword("secret");

        Mockito.when(authService.login("john", "secret"))
                .thenReturn(new AuthResponse("token", 123L, "USER"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @Disabled
    void register_happyPath_returnsUser() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("testuser");
        req.setEmail("t@example.com");
        req.setPassword("p");
        req.setFirstName("F");
        req.setLastName("L");
        req.setPhoneNumber("123");
        req.setBirthDate(ZonedDateTime.now().toString());
        req.setStreet("Street");
        req.setCity("City");
        req.setPostalCode("PC");
        req.setCountry("CZ");
        req.setRegion("PRAHA");

        RemaxUser saved = Mockito.mock(RemaxUser.class);
        Mockito.when(authService.register(any(RegisterRequest.class))).thenReturn(saved);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void me_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void requestPasswordReset_callsService_andReturnsGif() throws Exception {
        PasswordResetRequest req = new PasswordResetRequest();
        req.setEmail("a@b.com");

        mockMvc.perform(post("/api/auth/password-reset/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("giphy.gif")));

        Mockito.verify(authService).requestPasswordReset("a@b.com");
    }

    @Test
    void confirmPasswordReset_callsService() throws Exception {
        PasswordResetConfirmRequest req = new PasswordResetConfirmRequest();
        req.setUsername("u");
        req.setCode("c");
        req.setNewPassword("p");

        mockMvc.perform(post("/api/auth/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());

        Mockito.verify(authService).resetPassword("u", "c", "p");
    }
}
