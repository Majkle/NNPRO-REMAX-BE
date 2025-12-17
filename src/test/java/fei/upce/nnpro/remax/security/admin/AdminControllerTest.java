package fei.upce.nnpro.remax.security.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import fei.upce.nnpro.remax.profile.dto.RemaxUserResponse;
import fei.upce.nnpro.remax.profile.entity.Admin;
import fei.upce.nnpro.remax.profile.entity.Realtor;
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
    private ObjectMapper objectMapper;

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController)
//                .apply(springSecurity())
                .build();
        objectMapper = new ObjectMapper();
    }

    // Tests for blockUser endpoint - Success Cases
    @Test
    void blockUser_withNoUntil_usesDefaultAndReturnsOk() throws Exception {
        Admin a = new Admin();
        a.setUsername("testuser");
        Mockito.when(adminService.blockUser(eq("testuser"), any(ZonedDateTime.class))).thenReturn(a);

        mockMvc.perform(post("/api/admin/block/testuser").principal(new TestingAuthenticationToken("admin", "p", "ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void blockUser_withUntilParameter_passesParameterToService() throws Exception {
        Admin a = new Admin();
        a.setUsername("testuser");
        var blockUntil = ZonedDateTime.now().plusDays(7);
        Mockito.when(adminService.blockUser(eq("testuser"), any(ZonedDateTime.class))).thenReturn(a);

        mockMvc.perform(post("/api/admin/block/testuser")
                .param("until", blockUntil.toString())
                .principal(new TestingAuthenticationToken("admin", "p", "ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    // Tests for unblockUser endpoint - Success Case
    @Test
    void unblockUser_withValidUser_returnsOk() throws Exception {
        Admin a = new Admin();
        a.setUsername("testuser");
        Mockito.when(adminService.unblockUser("testuser")).thenReturn(a);

        mockMvc.perform(post("/api/admin/unblock/testuser").principal(new TestingAuthenticationToken("admin", "p", "ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    // Tests for getAllUsers endpoint - Success Cases
    @Test
    void getAllUsers_withMultipleUsers_returnsList() throws Exception {
        RemaxUserResponse user1 = new RemaxUserResponse();
        RemaxUserResponse user2 = new RemaxUserResponse();
        Mockito.when(adminService.listAllUsers()).thenReturn(List.of(user1, user2));

        mockMvc.perform(get("/api/admin/users").principal(new TestingAuthenticationToken("admin", "p", "ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    void getAllUsers_withNoUsers_returnsEmptyList() throws Exception {
        Mockito.when(adminService.listAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/users").principal(new TestingAuthenticationToken("admin", "p", "ROLE_ADMIN")))
                .andExpect(status().isOk());
    }

    // Tests for deleteUser endpoint - Success Case
    @Test
    void deleteUser_withValidUser_returnsNoContent() throws Exception {
        Mockito.doNothing().when(adminService).deleteUser("testuser");

        mockMvc.perform(delete("/api/admin/users/testuser").principal(new TestingAuthenticationToken("admin", "p", "ROLE_ADMIN")))
                .andExpect(status().isNoContent());

        Mockito.verify(adminService).deleteUser("testuser");
    }

    @Test
    void deleteUser_callsServiceWithCorrectUsername() throws Exception {
        Mockito.doNothing().when(adminService).deleteUser("john_doe");

        mockMvc.perform(delete("/api/admin/users/john_doe").principal(new TestingAuthenticationToken("admin", "p", "ROLE_ADMIN")))
                .andExpect(status().isNoContent());

        Mockito.verify(adminService).deleteUser("john_doe");
    }

    // Tests for updateUser endpoint - Success Cases
    @Test
    void updateUser_withValidRequest_returnsOk() throws Exception {
        UpdateUserRequest req = new UpdateUserRequest();
        req.setEmail("new@example.com");
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setDegree("Ing.");
        req.setPhoneNumber("+420123456789");
        req.setBirthDate(ZonedDateTime.now().toString());
        req.setStreet("Main Street 10");
        req.setCity("Prague");
        req.setPostalCode("11000");
        req.setCountry("CZ");
        req.setRegion("PRAHA");
        req.setLicenseNumber(12345);

        Admin a = new Admin();
        a.setUsername("testuser");
        a.setEmail("new@example.com");
        Mockito.when(adminService.updateUser(eq("testuser"), any(UpdateUserRequest.class))).thenReturn(a);

        mockMvc.perform(put("/api/admin/users/testuser")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(new TestingAuthenticationToken("admin", "p", "ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void updateUser_withValidRequestCallsService() throws Exception {
        UpdateUserRequest req = new UpdateUserRequest();
        req.setEmail("updated@example.com");
        req.setFirstName("Jane");
        req.setLastName("Smith");
        req.setDegree("Mgr.");
        req.setPhoneNumber("+420987654321");
        req.setBirthDate(ZonedDateTime.now().toString());
        req.setStreet("Updated Street 99");
        req.setCity("Brno");
        req.setPostalCode("60200");
        req.setCountry("CZ");
        req.setRegion("JIHOMORAVSKY");
        req.setLicenseNumber(54321);

        Admin a = new Admin();
        a.setUsername("existing_user");
        a.setEmail("updated@example.com");
        Mockito.when(adminService.updateUser(eq("existing_user"), any(UpdateUserRequest.class))).thenReturn(a);

        mockMvc.perform(put("/api/admin/users/existing_user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(new TestingAuthenticationToken("admin", "p", "ROLE_ADMIN")))
                .andExpect(status().isOk());

        Mockito.verify(adminService).updateUser(eq("existing_user"), any(UpdateUserRequest.class));
    }

    // Tests for createRealtor endpoint - Success Case
    @Test
    void createRealtor_withValidRequest_returnsCreated() throws Exception {
        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("realtor1");
        req.setEmail("realtor@example.com");
        req.setPassword("SecurePass123!");
        req.setLicenseNumber(123456);
        req.setAbout("Senior agent");
        req.setFirstName("Jane");
        req.setLastName("Smith");
        req.setDegree("Ing.");
        req.setPhoneNumber("+420123456789");
        req.setBirthDate(ZonedDateTime.now().toString());
        req.setStreet("Agent Street 5");
        req.setCity("Prague");
        req.setPostalCode("11000");
        req.setCountry("CZ");
        req.setRegion("PRAHA");

        Realtor realtor = new Realtor();
        realtor.setUsername("realtor1");
        realtor.setEmail("realtor@example.com");
        realtor.setLicenseNumber(123456);
        Mockito.when(adminService.createRealtor(any(CreateUserRequest.class))).thenReturn(realtor);

        mockMvc.perform(post("/api/admin/realtors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(new TestingAuthenticationToken("admin", "p", "ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("realtor1"));
    }

    @Test
    void createRealtor_withValidRequestCallsService() throws Exception {
        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("another_realtor");
        req.setEmail("another@realtors.com");
        req.setPassword("Pass1234!");
        req.setLicenseNumber(999888);
        req.setAbout("Experienced agent");
        req.setFirstName("John");
        req.setLastName("Brown");
        req.setDegree("Bc.");
        req.setPhoneNumber("+420555555555");
        req.setBirthDate(ZonedDateTime.now().toString());
        req.setStreet("New Street 123");
        req.setCity("Brno");
        req.setPostalCode("60200");
        req.setCountry("CZ");
        req.setRegion("JIHOMORAVSKY");

        Realtor realtor = new Realtor();
        realtor.setUsername("another_realtor");
        Mockito.when(adminService.createRealtor(any(CreateUserRequest.class))).thenReturn(realtor);

        mockMvc.perform(post("/api/admin/realtors")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(new TestingAuthenticationToken("admin", "p", "ROLE_ADMIN")))
                .andExpect(status().isOk());

        Mockito.verify(adminService).createRealtor(any(CreateUserRequest.class));
    }

    // Tests for createAdmin endpoint - Success Cases
    @Test
    void createAdmin_withValidRequest_returnsCreated() throws Exception {
        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("admin1");
        req.setEmail("admin@example.com");
        req.setPassword("AdminPass123!");
        req.setFirstName("Administrator");
        req.setLastName("User");
        req.setDegree("Mgr.");
        req.setPhoneNumber("+420999999999");
        req.setBirthDate(ZonedDateTime.now().toString());
        req.setStreet("Admin Street 1");
        req.setCity("Prague");
        req.setPostalCode("11000");
        req.setCountry("CZ");
        req.setRegion("PRAHA");

        Admin admin = new Admin();
        admin.setUsername("admin1");
        admin.setEmail("admin@example.com");
        Mockito.when(adminService.createAdmin(any(CreateUserRequest.class))).thenReturn(admin);

        mockMvc.perform(post("/api/admin/admins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(new TestingAuthenticationToken("admin", "p", "ROLE_ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin1"));
    }

    @Test
    void createAdmin_withValidRequestCallsService() throws Exception {
        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("super_admin");
        req.setEmail("superadmin@example.com");
        req.setPassword("SuperAdmin123!");
        req.setFirstName("Super");
        req.setLastName("Admin");
        req.setDegree("Ing.");
        req.setPhoneNumber("+420111111111");
        req.setBirthDate(ZonedDateTime.now().toString());
        req.setStreet("Admin Square 1");
        req.setCity("Prague");
        req.setPostalCode("11000");
        req.setCountry("CZ");
        req.setRegion("PRAHA");

        Admin admin = new Admin();
        admin.setUsername("super_admin");
        admin.setEmail("superadmin@example.com");
        Mockito.when(adminService.createAdmin(any(CreateUserRequest.class))).thenReturn(admin);

        mockMvc.perform(post("/api/admin/admins")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req))
                .principal(new TestingAuthenticationToken("admin", "p", "ROLE_ADMIN")))
                .andExpect(status().isOk());

        Mockito.verify(adminService).createAdmin(any(CreateUserRequest.class));
    }
}
