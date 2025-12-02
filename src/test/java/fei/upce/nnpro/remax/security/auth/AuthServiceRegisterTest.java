package fei.upce.nnpro.remax.security.auth;

import fei.upce.nnpro.remax.model.realestates.entity.Address;
import fei.upce.nnpro.remax.address.AddressService;
import fei.upce.nnpro.remax.model.users.PersonalInformation;
import fei.upce.nnpro.remax.model.users.RemaxUser;
import fei.upce.nnpro.remax.model.users.RemaxUserRepository;
import fei.upce.nnpro.remax.security.auth.request.RegisterRequest;
import fei.upce.nnpro.remax.security.config.SecurityProperties;
import fei.upce.nnpro.remax.security.jwt.JwtUtil;
import fei.upce.nnpro.remax.personalInformation.PersonalInformationService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceRegisterTest {

    @Test
    void registerCreatesUserAddressAndPersonalInfo() {
        // mock repositories and dependencies
        RemaxUserRepository userRepo = Mockito.mock(RemaxUserRepository.class);
        AddressService addressService = Mockito.mock(AddressService.class);
        PersonalInformationService piService = Mockito.mock(PersonalInformationService.class);
        AuthenticationManager authManager = Mockito.mock(AuthenticationManager.class);
        JwtUtil jwt = Mockito.mock(JwtUtil.class);
        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);
        SecurityProperties props = new SecurityProperties();
        props.setJwtExpirationMs(3600000);

        AuthService service = new AuthService(userRepo, addressService, piService, authManager, jwt, encoder, props);

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

        Mockito.when(userRepo.findByUsername("testuser")).thenReturn(Optional.empty());
        Mockito.when(userRepo.findByEmail("t@example.com")).thenReturn(Optional.empty());

        Mockito.when(addressService.createFrom(Mockito.any(RegisterRequest.class))).thenAnswer(i -> {
            Address a = new Address();
            a.setId(1L);
            a.setStreet(((RegisterRequest)i.getArgument(0)).getStreet());
            return a;
        });

        // createFrom returns a PersonalInformation (persisted by createFrom in service implementation)
        Mockito.when(piService.createFrom(Mockito.any(RegisterRequest.class), Mockito.any(Address.class))).thenAnswer(i -> {
            PersonalInformation p = new PersonalInformation();
            p.setFirstName(((RegisterRequest)i.getArgument(0)).getFirstName());
            p.setLastName(((RegisterRequest)i.getArgument(0)).getLastName());
            return p;
        });

        Mockito.when(piService.save(Mockito.any(PersonalInformation.class))).thenAnswer(i -> {
            PersonalInformation p = i.getArgument(0);
            p.setId(2L);
            return p;
        });

        Mockito.when(encoder.encode(Mockito.anyString())).thenReturn("encoded");
        Mockito.when(userRepo.save(Mockito.any(RemaxUser.class))).thenAnswer(i -> {
            RemaxUser u = i.getArgument(0);
            u.setId(3L);
            return u;
        });

        RemaxUser created = service.register(req);
        assertNotNull(created);
        assertEquals(3L, created.getId());
        assertEquals("testuser", created.getUsername());
        assertEquals("t@example.com", created.getEmail());
        assertEquals("encoded", created.getPassword());

        Mockito.verify(addressService).createFrom(Mockito.any(RegisterRequest.class));
        Mockito.verify(piService).createFrom(Mockito.any(RegisterRequest.class), Mockito.any(Address.class));
        Mockito.verify(piService).save(Mockito.any(PersonalInformation.class));
        Mockito.verify(userRepo).save(Mockito.any(RemaxUser.class));
    }
}
