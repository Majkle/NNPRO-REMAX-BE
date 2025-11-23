package fei.upce.nnprop.remax.security.service;

import fei.upce.nnprop.remax.model.users.Client;
import fei.upce.nnprop.remax.model.users.RemaxUserRepository;
import fei.upce.nnprop.remax.model.users.PersonalInformation;
import fei.upce.nnprop.remax.model.users.enums.AccountStatus;
import fei.upce.nnprop.remax.security.auth.CustomUserDetailsService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class CustomUserDetailsServiceTest {

    @Test
    void loadExistingUser() {
        RemaxUserRepository repo = Mockito.mock(RemaxUserRepository.class);

        Client u = new Client();
        u.setUsername("alice");
        u.setPassword("secret");
        u.setAccountStatus(AccountStatus.NORMAL);
        u.setEmail("alice@example.com");
        u.setCreatedAt(OffsetDateTime.now());
        PersonalInformation pi = new PersonalInformation();
        pi.setFirstName("Alice");
        pi.setLastName("User");
        pi.setPhoneNumber("123");
        pi.setBirthDate(ZonedDateTime.now());
        u.setPersonalInformation(pi);

        Mockito.when(repo.findByUsername("alice")).thenReturn(Optional.of(u));

        CustomUserDetailsService s = new CustomUserDetailsService(repo);
        UserDetails details = s.loadUserByUsername("alice");
        assertEquals("alice", details.getUsername());
        assertEquals("secret", details.getPassword());
    }

    @Test
    void nonExistingUserThrows() {
        RemaxUserRepository repo = Mockito.mock(RemaxUserRepository.class);
        Mockito.when(repo.findByUsername("missing")).thenReturn(Optional.empty());
        CustomUserDetailsService s = new CustomUserDetailsService(repo);
        assertThrows(UsernameNotFoundException.class, () -> s.loadUserByUsername("missing"));
    }
}
