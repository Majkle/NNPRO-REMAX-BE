package fei.upce.nnpro.remax.security.config;

import fei.upce.nnpro.remax.address.entity.Address;
import fei.upce.nnpro.remax.address.service.AddressService;
import fei.upce.nnpro.remax.profile.entity.Admin;
import fei.upce.nnpro.remax.profile.entity.PersonalInformation;
import fei.upce.nnpro.remax.profile.repository.RemaxUserRepository;
import fei.upce.nnpro.remax.profile.service.PersonalInformationService;
import fei.upce.nnpro.remax.realestates.entity.enums.AddressRegion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

@Configuration
@Slf4j
public class AdminInitializer {
    @Value("${remax.default-admin.username}")
    private String username;

    private String email = "admin@remax.cz";

    @Value("${remax.default-admin.password}")
    private String password;

    @Bean
    public ApplicationRunner applicationRunner(
            RemaxUserRepository userRepository,
            AddressService addressService,
            PersonalInformationService personalInformationService,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            if (!userRepository.existsByUsernameOrEmail(username, email)) {
                log.info("Registering default admin...");

                Address address = new Address();
                address.setStreet("Československých legií 565");
                address.setCity("Pardubice");
                address.setPostalCode("530 02");
                address.setCountry("Česká republika");
                address.setRegion(AddressRegion.PARDUBICKY);
                address = addressService.save(address);

                PersonalInformation pi = new PersonalInformation();
                pi.setAddress(address);
                pi.setDegree("Ing.");
                pi.setFirstName("Jan");
                pi.setLastName("Novák");
                pi.setPhoneNumber("+420 321 654 987");
                pi.setBirthDate(ZonedDateTime.now());
                pi = personalInformationService.save(pi);

                Admin admin = new Admin();
                admin.setUsername(username);
                admin.setEmail(email);
                admin.setPassword(passwordEncoder.encode(password));
                admin.setPersonalInformation(pi);
                admin.setCreatedAt(OffsetDateTime.now());

                userRepository.save(admin);
                log.info("Registered default admin username={} id={}", admin.getUsername(), admin.getId());
            }
        };
    }
}
