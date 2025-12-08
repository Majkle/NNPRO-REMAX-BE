package fei.upce.nnpro.remax.profile.service;

import fei.upce.nnpro.remax.address.entity.Address;
import fei.upce.nnpro.remax.profile.entity.PersonalInformation;
import fei.upce.nnpro.remax.profile.repository.PersonalInformationRepository;
import fei.upce.nnpro.remax.security.auth.request.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonalInformationServiceTest {

    @Mock
    private PersonalInformationRepository repo;

    private PersonalInformationService sut;

    @BeforeEach
    void setUp() {
        sut = new PersonalInformationService(repo);
    }

    @Test
    void save_shouldDelegate() {
        PersonalInformation pi = new PersonalInformation();
        pi.setFirstName("A");
        PersonalInformation out = new PersonalInformation();
        out.setId(5L);
        when(repo.save(pi)).thenReturn(out);

        PersonalInformation saved = sut.save(pi);
        assertThat(saved).isSameAs(out);
    }

    @Test
    void createFrom_shouldParseBirthDateOrThrow() {
        RegisterRequest r = new RegisterRequest();
        r.setFirstName("F");
        r.setLastName("L");
        r.setPhoneNumber("123");
        r.setBirthDate(ZonedDateTime.now().toString());
        Address a = new Address();
        PersonalInformation out = new PersonalInformation();
        out.setId(7L);
        when(repo.save(org.mockito.Mockito.any())).thenReturn(out);

        PersonalInformation res = sut.createFrom(r, a);
        assertThat(res).isSameAs(out);

        r.setBirthDate("not-a-date");
        assertThatThrownBy(() -> sut.createFrom(r, a)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid birthDate format");
    }
}

