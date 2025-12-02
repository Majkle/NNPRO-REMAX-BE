package fei.upce.nnpro.remax.security.auth;

import fei.upce.nnpro.remax.address.AddressService;
import fei.upce.nnpro.remax.model.users.RemaxUser;
import fei.upce.nnpro.remax.model.users.RemaxUserRepository;
import fei.upce.nnpro.remax.personalInformation.PersonalInformationService;
import fei.upce.nnpro.remax.security.config.SecurityProperties;
import fei.upce.nnpro.remax.security.jwt.JwtUtil;
import fei.upce.nnpro.remax.mail.MailService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class AuthServicePasswordResetTest {

    @Test
    void requestPasswordReset_sendsEmailAndSavesCode() {
        RemaxUserRepository userRepo = Mockito.mock(RemaxUserRepository.class);
        AddressService addressService = Mockito.mock(AddressService.class);
        PersonalInformationService piService = Mockito.mock(PersonalInformationService.class);
        AuthenticationManager authManager = Mockito.mock(AuthenticationManager.class);
        JwtUtil jwt = Mockito.mock(JwtUtil.class);
        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);
        SecurityProperties props = new SecurityProperties();
        MailService mailService = Mockito.mock(MailService.class);

        AuthService svc = new AuthService(userRepo, addressService, piService, authManager, jwt, encoder, props, mailService);

        RemaxUser user = new RemaxUser(){};
        user.setEmail("u@example.com");

        when(userRepo.findByEmail("u@example.com")).thenReturn(Optional.of(user));
        when(userRepo.save(any(RemaxUser.class))).thenAnswer(i -> i.getArgument(0));

        svc.requestPasswordReset("u@example.com");

        verify(mailService, times(1)).sendPasswordResetCode(eq("u@example.com"), anyString());
        verify(userRepo, times(1)).save(any(RemaxUser.class));
        assertNotNull(user.getPasswordResetCode());
        assertNotNull(user.getPasswordResetCodeDeadline());
        assertTrue(user.getPasswordResetCodeDeadline().isAfter(ZonedDateTime.now()));
    }

    @Test
    void resetPassword_withValidCode_resetsPassword() {
        RemaxUserRepository userRepo = Mockito.mock(RemaxUserRepository.class);
        AddressService addressService = Mockito.mock(AddressService.class);
        PersonalInformationService piService = Mockito.mock(PersonalInformationService.class);
        AuthenticationManager authManager = Mockito.mock(AuthenticationManager.class);
        JwtUtil jwt = Mockito.mock(JwtUtil.class);
        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);
        SecurityProperties props = new SecurityProperties();
        MailService mailService = Mockito.mock(MailService.class);

        AuthService svc = new AuthService(userRepo, addressService, piService, authManager, jwt, encoder, props, mailService);

        RemaxUser user = new RemaxUser(){};
        user.setEmail("u@example.com");
        user.setPasswordResetCode("ABC12345");
        user.setPasswordResetCodeDeadline(ZonedDateTime.now().plusHours(1));

        when(userRepo.findAll()).thenReturn(List.of(user));
        when(encoder.encode("newpass")).thenReturn("encoded");
        when(userRepo.save(any(RemaxUser.class))).thenAnswer(i -> i.getArgument(0));

        svc.resetPassword("ABC12345", "newpass");

        verify(encoder).encode("newpass");
        assertEquals("encoded", user.getPassword());
        assertNull(user.getPasswordResetCode());
        assertNull(user.getPasswordResetCodeDeadline());
    }

    @Test
    void resetPassword_invalidCode_throws() {
        RemaxUserRepository userRepo = Mockito.mock(RemaxUserRepository.class);
        AddressService addressService = Mockito.mock(AddressService.class);
        PersonalInformationService piService = Mockito.mock(PersonalInformationService.class);
        AuthenticationManager authManager = Mockito.mock(AuthenticationManager.class);
        JwtUtil jwt = Mockito.mock(JwtUtil.class);
        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);
        SecurityProperties props = new SecurityProperties();
        MailService mailService = Mockito.mock(MailService.class);

        AuthService svc = new AuthService(userRepo, addressService, piService, authManager, jwt, encoder, props, mailService);

        when(userRepo.findAll()).thenReturn(List.of());

        assertThrows(IllegalArgumentException.class, () -> svc.resetPassword("NOPE", "p"));
    }

    @Test
    void resetPassword_expiredCode_throws() {
        RemaxUserRepository userRepo = Mockito.mock(RemaxUserRepository.class);
        AddressService addressService = Mockito.mock(AddressService.class);
        PersonalInformationService piService = Mockito.mock(PersonalInformationService.class);
        AuthenticationManager authManager = Mockito.mock(AuthenticationManager.class);
        JwtUtil jwt = Mockito.mock(JwtUtil.class);
        PasswordEncoder encoder = Mockito.mock(PasswordEncoder.class);
        SecurityProperties props = new SecurityProperties();
        MailService mailService = Mockito.mock(MailService.class);

        AuthService svc = new AuthService(userRepo, addressService, piService, authManager, jwt, encoder, props, mailService);

        RemaxUser user = new RemaxUser(){};
        user.setPasswordResetCode("OLD12345");
        user.setPasswordResetCodeDeadline(ZonedDateTime.now().minusHours(1));

        when(userRepo.findAll()).thenReturn(List.of(user));

        assertThrows(IllegalArgumentException.class, () -> svc.resetPassword("OLD12345", "p"));
    }
}

