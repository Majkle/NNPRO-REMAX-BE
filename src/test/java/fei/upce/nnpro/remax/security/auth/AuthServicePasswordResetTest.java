package fei.upce.nnpro.remax.security.auth;

import fei.upce.nnpro.remax.address.service.AddressService;
import fei.upce.nnpro.remax.mail.service.MailService;
import fei.upce.nnpro.remax.profile.entity.RemaxUser;
import fei.upce.nnpro.remax.profile.repository.RemaxUserRepository;
import fei.upce.nnpro.remax.profile.service.PersonalInformationService;
import fei.upce.nnpro.remax.security.auth.service.AuthService;
import fei.upce.nnpro.remax.security.config.SecurityProperties;
import fei.upce.nnpro.remax.security.jwt.JwtUtil;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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
    void requestPasswordReset_sendsEmailAndSavesEncodedCode() {
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
        // simulate encoder producing encoded value
        when(encoder.encode(anyString())).thenAnswer(i -> "ENC:" + i.getArgument(0));

        svc.requestPasswordReset("u@example.com");

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailService, times(1)).sendPasswordResetCode(eq("u@example.com"), codeCaptor.capture());
        String rawSent = codeCaptor.getValue();
        assertNotNull(rawSent);

        verify(userRepo, times(1)).save(any(RemaxUser.class));
        assertNotNull(user.getPasswordResetCode());
        // ensure stored code is encoded, not equal to raw
        assertNotEquals(rawSent, user.getPasswordResetCode());
        assertTrue(user.getPasswordResetCode().startsWith("ENC:"));
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

        RemaxUser user = new RemaxUser() {
        };
        user.setUsername("pepazdepa");
        user.setEmail("u@example.com");
        // store encoded code as it would be in DB
        when(encoder.matches(eq("ABC12345"), anyString())).thenReturn(true);
        user.setPasswordResetCode("ENC_ABC");
        user.setPasswordResetCodeDeadline(ZonedDateTime.now().plusHours(1));

        when(userRepo.findByUsername("pepazdepa")).thenReturn(Optional.of(user));
        when(encoder.encode("newpass")).thenReturn("encoded");
        when(userRepo.save(any(RemaxUser.class))).thenAnswer(i -> i.getArgument(0));

        svc.resetPassword("pepazdepa", "ABC12345", "newpass");

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

        assertThrows(IllegalArgumentException.class, () -> svc.resetPassword("pepazdepa", "NOPE", "p"));
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

        RemaxUser user = new RemaxUser() {
        };
        user.setUsername("pepazdepa");
        user.setPasswordResetCode("OLD12345");
        user.setPasswordResetCodeDeadline(ZonedDateTime.now().minusHours(1));

        when(userRepo.findAll()).thenReturn(List.of(user));

        assertThrows(IllegalArgumentException.class, () -> svc.resetPassword("pepazdepa", "OLD12345", "p"));
    }
}
