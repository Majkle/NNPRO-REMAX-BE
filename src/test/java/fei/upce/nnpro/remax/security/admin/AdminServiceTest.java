package fei.upce.nnpro.remax.security.admin;

import fei.upce.nnpro.remax.address.entity.Address;
import fei.upce.nnpro.remax.profile.entity.enums.AccountStatus;
import fei.upce.nnpro.remax.address.service.AddressService;
import fei.upce.nnpro.remax.profile.entity.PersonalInformation;
import fei.upce.nnpro.remax.profile.entity.Realtor;
import fei.upce.nnpro.remax.profile.entity.RemaxUser;
import fei.upce.nnpro.remax.profile.repository.RemaxUserRepository;
import fei.upce.nnpro.remax.profile.service.PersonalInformationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AdminServiceTest {

    @Test
    void blockUser() {
        RemaxUserRepository userRepo = Mockito.mock(RemaxUserRepository.class);
        AddressService addressService = Mockito.mock(AddressService.class);
        PersonalInformationService piService = Mockito.mock(PersonalInformationService.class);
        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);

        AdminService svc = new AdminService(userRepo, addressService, piService, encoder);

        RemaxUser user = new RemaxUser() {};
        user.setUsername("u");
        Mockito.when(userRepo.findByUsername("u")).thenReturn(Optional.of(user));
        Mockito.when(userRepo.save(Mockito.any(RemaxUser.class))).thenAnswer(i -> i.getArgument(0));

        var until = ZonedDateTime.now().plusDays(1);
        var updated = svc.blockUser("u", until);
        assertEquals(updated.getAccountStatus().name(), "BLOCKED");
        assertNotNull(updated.getBlockedUntil());
    }

    @Test
    void unblockUser() {
        RemaxUserRepository userRepo = Mockito.mock(RemaxUserRepository.class);
        AddressService addressService = Mockito.mock(AddressService.class);
        PersonalInformationService piService = Mockito.mock(PersonalInformationService.class);
        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);

        AdminService svc = new AdminService(userRepo, addressService, piService, encoder);

        RemaxUser user = new RemaxUser() {};
        user.setUsername("u");
        user.setAccountStatus(AccountStatus.BLOCKED);
        user.setFailedLoginAttempts(2);
        user.setBlockedUntil(ZonedDateTime.now().plusDays(1));

        Mockito.when(userRepo.findByUsername("u")).thenReturn(Optional.of(user));
        Mockito.when(userRepo.save(Mockito.any(RemaxUser.class))).thenAnswer(i -> i.getArgument(0));

        var updated = svc.unblockUser("u");
        assertEquals(AccountStatus.NORMAL, updated.getAccountStatus());
        assertEquals(0, updated.getFailedLoginAttempts());
        assertNull(updated.getBlockedUntil());
    }

    @Test
    void deleteUser() {
        RemaxUserRepository userRepo = Mockito.mock(RemaxUserRepository.class);
        AddressService addressService = Mockito.mock(AddressService.class);
        PersonalInformationService piService = Mockito.mock(PersonalInformationService.class);
        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);

        AdminService svc = new AdminService(userRepo, addressService, piService, encoder);

        RemaxUser user = new RemaxUser() {};
        user.setUsername("u");
        Mockito.when(userRepo.findByUsername("u")).thenReturn(Optional.of(user));
        Mockito.doNothing().when(userRepo).delete(Mockito.any(RemaxUser.class));

        svc.deleteUser("u");
        Mockito.verify(userRepo).delete(Mockito.any(RemaxUser.class));
    }

    @Test
    void createRealtor() {
        RemaxUserRepository userRepo = Mockito.mock(RemaxUserRepository.class);
        AddressService addressService = Mockito.mock(AddressService.class);
        PersonalInformationService piService = Mockito.mock(PersonalInformationService.class);
        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);

        AdminService svc = new AdminService(userRepo, addressService, piService, encoder);

        CreateUserRequest req = new CreateUserRequest();
        req.setUsername("r");
        req.setEmail("r@example.com");
        req.setPassword("p");
        req.setLicenseNumber(123);
        req.setFirstName("F");
        req.setLastName("L");
        req.setPhoneNumber("123");
        req.setBirthDate(ZonedDateTime.now().toString());
        req.setStreet("S");
        req.setCity("C");
        req.setPostalCode("PC");
        req.setCountry("CZ");
        req.setRegion("PRAHA");

        Mockito.when(userRepo.findByUsername("r")).thenReturn(Optional.empty());
        Mockito.when(userRepo.findByEmail("r@example.com")).thenReturn(Optional.empty());

        Mockito.when(addressService.createFrom(Mockito.any())).thenAnswer(i -> {
            Address a = new Address(); a.setId(1L); return a;
        });
        Mockito.when(piService.createFrom(Mockito.any(), Mockito.any(Address.class))).thenAnswer(i -> {
            PersonalInformation p = new PersonalInformation(); p.setId(2L); return p;
        });
        Mockito.when(piService.save(Mockito.any(PersonalInformation.class))).thenAnswer(i -> i.getArgument(0));
        Mockito.when(encoder.encode(Mockito.anyString())).thenReturn("enc");
        Mockito.when(userRepo.save(Mockito.any(Realtor.class))).thenAnswer(i -> i.getArgument(0));

        Realtor r = svc.createRealtor(req);
        assertNotNull(r);
        assertEquals(123, r.getLicenseNumber());
        assertEquals("enc", r.getPassword());
    }
}


