package fei.upce.nnpro.remax.security.admin;

import fei.upce.nnpro.remax.address.entity.Address;
import fei.upce.nnpro.remax.address.service.AddressService;
import fei.upce.nnpro.remax.profile.dto.RemaxUserResponse;
import fei.upce.nnpro.remax.profile.entity.Admin;
import fei.upce.nnpro.remax.profile.entity.PersonalInformation;
import fei.upce.nnpro.remax.profile.entity.Realtor;
import fei.upce.nnpro.remax.profile.entity.RemaxUser;
import fei.upce.nnpro.remax.profile.entity.enums.AccountStatus;
import fei.upce.nnpro.remax.profile.repository.RemaxUserRepository;
import fei.upce.nnpro.remax.profile.service.PersonalInformationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AdminServiceTest {

    private RemaxUserRepository userRepo;
    private AddressService addressService;
    private PersonalInformationService piService;
    private PasswordEncoder encoder;
    private AdminService adminService;

    @BeforeEach
    void setUp() {
        userRepo = Mockito.mock(RemaxUserRepository.class);
        addressService = Mockito.mock(AddressService.class);
        piService = Mockito.mock(PersonalInformationService.class);
        encoder = Mockito.mock(PasswordEncoder.class);
        adminService = new AdminService(userRepo, addressService, piService, encoder);
    }

    // Tests for blockUser
    @Test
    void blockUser_withValidUser_returnsBlockedUser() {
        RemaxUser user = new RemaxUser() {};
        user.setUsername("testuser");
        Mockito.when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(user));
        Mockito.when(userRepo.save(Mockito.any(RemaxUser.class))).thenAnswer(i -> i.getArgument(0));

        var until = ZonedDateTime.now().plusDays(1);
        var updated = adminService.blockUser("testuser", until);

        assertEquals(AccountStatus.BLOCKED, updated.getAccountStatus());
        assertNotNull(updated.getBlockedUntil());
        assertEquals(until, updated.getBlockedUntil());
    }

    @Test
    void blockUser_withNonExistentUser_throwsException() {
        Mockito.when(userRepo.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> adminService.blockUser("nonexistent", ZonedDateTime.now()));
    }

    @Test
    void blockUser_callsSaveWithUpdatedUser() {
        RemaxUser user = new RemaxUser() {};
        user.setUsername("testuser");
        Mockito.when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(user));
        Mockito.when(userRepo.save(Mockito.any(RemaxUser.class))).thenAnswer(i -> i.getArgument(0));

        var until = ZonedDateTime.now().plusDays(7);
        adminService.blockUser("testuser", until);

        Mockito.verify(userRepo).save(Mockito.any(RemaxUser.class));
    }

    // Tests for unblockUser
    @Test
    void unblockUser_withBlockedUser_returnsUnblockedUser() {
        RemaxUser user = new RemaxUser() {};
        user.setUsername("testuser");
        user.setAccountStatus(AccountStatus.BLOCKED);
        user.setFailedLoginAttempts(3);
        user.setBlockedUntil(ZonedDateTime.now().plusDays(1));

        Mockito.when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(user));
        Mockito.when(userRepo.save(Mockito.any(RemaxUser.class))).thenAnswer(i -> i.getArgument(0));

        var updated = adminService.unblockUser("testuser");

        assertEquals(AccountStatus.NORMAL, updated.getAccountStatus());
        assertEquals(0, updated.getFailedLoginAttempts());
        assertNull(updated.getBlockedUntil());
    }

    @Test
    void unblockUser_withNonExistentUser_throwsException() {
        Mockito.when(userRepo.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> adminService.unblockUser("nonexistent"));
    }

    // Tests for deleteUser
    @Test
    void deleteUser_withValidUser_deletesUser() {
        RemaxUser user = new RemaxUser() {};
        user.setUsername("testuser");
        Mockito.when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(user));

        adminService.deleteUser("testuser");

        Mockito.verify(userRepo).delete(user);
    }

    @Test
    void deleteUser_withNonExistentUser_throwsException() {
        Mockito.when(userRepo.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> adminService.deleteUser("nonexistent"));
    }

    // Tests for updateUser
    @Test
    void updateUser_withValidUserAndRequest_returnsUpdatedUser() {
        RemaxUser user = new RemaxUser() {};
        user.setUsername("testuser");
        user.setEmail("old@example.com");

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

        Mockito.when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(user));
        Mockito.when(userRepo.findByEmail("new@example.com")).thenReturn(Optional.empty());

        Address address = new Address();
        Mockito.when(addressService.save(Mockito.any(Address.class))).thenAnswer(i -> i.getArgument(0));

        PersonalInformation pi = new PersonalInformation();
        Mockito.when(piService.save(Mockito.any(PersonalInformation.class))).thenAnswer(i -> i.getArgument(0));

        Mockito.when(userRepo.save(Mockito.any(RemaxUser.class))).thenAnswer(i -> i.getArgument(0));

        RemaxUser updated = adminService.updateUser("testuser", req);

        assertNotNull(updated);
        assertEquals("new@example.com", updated.getEmail());
    }

    @Test
    void updateUser_withNonExistentUser_throwsException() {
        Mockito.when(userRepo.findByUsername("nonexistent")).thenReturn(Optional.empty());

        UpdateUserRequest req = new UpdateUserRequest();
        assertThrows(IllegalArgumentException.class, () -> adminService.updateUser("nonexistent", req));
    }

    @Test
    void updateUser_withDuplicateEmail_throwsException() {
        RemaxUser user = new RemaxUser() {};
        user.setUsername("testuser");
        user.setEmail("old@example.com");

        UpdateUserRequest req = new UpdateUserRequest();
        req.setEmail("existing@example.com");

        Mockito.when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(user));
        Mockito.when(userRepo.findByEmail("existing@example.com")).thenReturn(Optional.of(new RemaxUser() {}));

        assertThrows(IllegalArgumentException.class, () -> adminService.updateUser("testuser", req));
    }

    @Test
    void updateUser_withInvalidRegion_throwsException() {
        RemaxUser user = new RemaxUser() {};
        user.setUsername("testuser");
        user.setEmail("old@example.com");

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
        req.setRegion("INVALID_REGION");
        req.setLicenseNumber(12345);

        Mockito.when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(user));
        Mockito.when(userRepo.findByEmail("new@example.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> adminService.updateUser("testuser", req));
    }

    @Test
    void updateUser_withInvalidBirthDate_throwsException() {
        RemaxUser user = new RemaxUser() {};
        user.setUsername("testuser");
        user.setEmail("old@example.com");

        UpdateUserRequest req = new UpdateUserRequest();
        req.setEmail("new@example.com");
        req.setFirstName("John");
        req.setLastName("Doe");
        req.setDegree("Ing.");
        req.setPhoneNumber("+420123456789");
        req.setBirthDate("INVALID_DATE");
        req.setStreet("Main Street 10");
        req.setCity("Prague");
        req.setPostalCode("11000");
        req.setCountry("CZ");
        req.setRegion("PRAHA");
        req.setLicenseNumber(12345);

        Mockito.when(userRepo.findByUsername("testuser")).thenReturn(Optional.of(user));
        Mockito.when(userRepo.findByEmail("new@example.com")).thenReturn(Optional.empty());
        Mockito.when(addressService.save(Mockito.any(Address.class))).thenAnswer(i -> i.getArgument(0));
        Mockito.when(piService.save(Mockito.any(PersonalInformation.class))).thenAnswer(i -> i.getArgument(0));

        assertThrows(IllegalArgumentException.class, () -> adminService.updateUser("testuser", req));
    }

    // Tests for createRealtor
    @Test
    void createRealtor_withValidRequest_returnsCreatedRealtor() {
        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("realtor1");
        req.setEmail("realtor@example.com");
        req.setPassword("SecurePass123!");
        req.setLicenseNumber(123456);
        req.setAbout("Senior agent with 10 years experience");
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

        Mockito.when(userRepo.findByUsername("realtor1")).thenReturn(Optional.empty());
        Mockito.when(userRepo.findByEmail("realtor@example.com")).thenReturn(Optional.empty());

        Address address = new Address();
        Mockito.when(addressService.createFrom(Mockito.any())).thenReturn(address);

        PersonalInformation pi = new PersonalInformation();
        Mockito.when(piService.createFrom(Mockito.any(), Mockito.any(Address.class))).thenReturn(pi);
        Mockito.when(piService.save(Mockito.any(PersonalInformation.class))).thenReturn(pi);

        Mockito.when(encoder.encode("SecurePass123!")).thenReturn("encoded_password");

        Mockito.when(userRepo.save(Mockito.any(Realtor.class))).thenAnswer(i -> i.getArgument(0));

        Realtor created = adminService.createRealtor(req);

        assertNotNull(created);
        assertEquals("realtor1", created.getUsername());
        assertEquals("realtor@example.com", created.getEmail());
        assertEquals(123456, created.getLicenseNumber());
        assertEquals("encoded_password", created.getPassword());
    }

    @Test
    void createRealtor_withDuplicateUsername_throwsException() {
        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("existing");
        req.setEmail("realtor@example.com");

        Mockito.when(userRepo.findByUsername("existing")).thenReturn(Optional.of(new RemaxUser() {}));

        assertThrows(IllegalArgumentException.class, () -> adminService.createRealtor(req));
    }

    @Test
    void createRealtor_withDuplicateEmail_throwsException() {
        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("realtor1");
        req.setEmail("existing@example.com");

        Mockito.when(userRepo.findByUsername("realtor1")).thenReturn(Optional.empty());
        Mockito.when(userRepo.findByEmail("existing@example.com")).thenReturn(Optional.of(new RemaxUser() {}));

        assertThrows(IllegalArgumentException.class, () -> adminService.createRealtor(req));
    }

    // Tests for createAdmin
    @Test
    void createAdmin_withValidRequest_returnsCreatedAdmin() {
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

        Mockito.when(userRepo.findByUsername("admin1")).thenReturn(Optional.empty());
        Mockito.when(userRepo.findByEmail("admin@example.com")).thenReturn(Optional.empty());

        Address address = new Address();
        Mockito.when(addressService.createFrom(Mockito.any())).thenReturn(address);

        PersonalInformation pi = new PersonalInformation();
        Mockito.when(piService.createFrom(Mockito.any(), Mockito.any(Address.class))).thenReturn(pi);
        Mockito.when(piService.save(Mockito.any(PersonalInformation.class))).thenReturn(pi);

        Mockito.when(encoder.encode("AdminPass123!")).thenReturn("encoded_admin_password");

        Mockito.when(userRepo.save(Mockito.any(Admin.class))).thenAnswer(i -> i.getArgument(0));

        Admin created = adminService.createAdmin(req);

        assertNotNull(created);
        assertEquals("admin1", created.getUsername());
        assertEquals("admin@example.com", created.getEmail());
        assertEquals("encoded_admin_password", created.getPassword());
    }

    @Test
    void createAdmin_withDuplicateUsername_throwsException() {
        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("existing");
        req.setEmail("admin@example.com");

        Mockito.when(userRepo.findByUsername("existing")).thenReturn(Optional.of(new RemaxUser() {}));

        assertThrows(IllegalArgumentException.class, () -> adminService.createAdmin(req));
    }

    @Test
    void createAdmin_withDuplicateEmail_throwsException() {
        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("admin1");
        req.setEmail("existing@example.com");

        Mockito.when(userRepo.findByUsername("admin1")).thenReturn(Optional.empty());
        Mockito.when(userRepo.findByEmail("existing@example.com")).thenReturn(Optional.of(new RemaxUser() {}));

        assertThrows(IllegalArgumentException.class, () -> adminService.createAdmin(req));
    }

    // Tests for listAllUsers
    @Test
    void listAllUsers_withMultipleUsers_returnsListWithMaskedPasswords() {
        RemaxUser user1 = new RemaxUser() {};
        user1.setUsername("user1");
        user1.setPassword("actualpassword1");

        RemaxUser user2 = new RemaxUser() {};
        user2.setUsername("user2");
        user2.setPassword("actualpassword2");

        List<RemaxUser> users = new ArrayList<>();
        users.add(user1);
        users.add(user2);

        Mockito.when(userRepo.findAll()).thenReturn(users);

        List<RemaxUserResponse> result = adminService.listAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    void listAllUsers_withNoUsers_returnsEmptyList() {
        Mockito.when(userRepo.findAll()).thenReturn(new ArrayList<>());

        List<RemaxUserResponse> result = adminService.listAllUsers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}


