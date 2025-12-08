package fei.upce.nnpro.remax.security.admin;

import fei.upce.nnpro.remax.profile.dto.RemaxUserResponse;
import fei.upce.nnpro.remax.profile.entity.Admin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.ZonedDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController)
//                .apply(springSecurity())
                .build();
    }

    @Test
    void blockUser_withNoUntil_usesDefaultAndReturnsOk() throws Exception {
        Admin a = new Admin();
        a.setUsername("u");
        Mockito.when(adminService.blockUser(eq("u"), any(ZonedDateTime.class))).thenReturn(a);

        mockMvc.perform(post("/api/admin/block/u").principal(new TestingAuthenticationToken("admin", "p", "ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("u"));
    }

    @Test
    void getAllUsers_returnsList() throws Exception {
        RemaxUserResponse r = new RemaxUserResponse();
        Mockito.when(adminService.listAllUsers()).thenReturn(List.of(r));

        mockMvc.perform(get("/api/admin/users").principal(new TestingAuthenticationToken("admin", "p", "ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void deleteUser_callsService_andReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/admin/users/j").principal(new TestingAuthenticationToken("admin", "p", "ROLE_ADMIN")))
                .andExpect(status().isNoContent());

        Mockito.verify(adminService).deleteUser("j");
    }
}
