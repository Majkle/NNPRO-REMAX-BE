package fei.upce.nnpro.remax.security.config;

import fei.upce.nnpro.remax.address.entity.Address;
import fei.upce.nnpro.remax.address.service.AddressService;
import fei.upce.nnpro.remax.profile.entity.Admin;
import fei.upce.nnpro.remax.profile.entity.Client;
import fei.upce.nnpro.remax.profile.entity.PersonalInformation;
import fei.upce.nnpro.remax.profile.entity.Realtor;
import fei.upce.nnpro.remax.profile.entity.enums.AccountStatus;
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

    // Admin properties
    @Value("${remax.default-admin.username}")
    private String adminUsername;
    @Value("${remax.default-admin.password}")
    private String adminPassword;
    private final String adminEmail = "admin@remax.XXX";

    // Test Realtor constants
    private final String realtorUsername = "realtor";
    private final String realtorPassword = "password";
    private final String realtorEmail = "realtor@remax.XXX";

    // Test Client constants
    private final String clientUsername = "client";
    private final String clientPassword = "password";
    private final String clientEmail = "client@remax.XXX";

    @Bean
    public ApplicationRunner applicationRunner(
            RemaxUserRepository userRepository,
            AddressService addressService,
            PersonalInformationService personalInformationService,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            createAdminIfNotFound(userRepository, addressService, personalInformationService, passwordEncoder);
            createRealtorIfNotFound(userRepository, addressService, personalInformationService, passwordEncoder);
            createClientIfNotFound(userRepository, addressService, personalInformationService, passwordEncoder);
        };
    }

    private void createAdminIfNotFound(RemaxUserRepository userRepository, AddressService addressService,
                                       PersonalInformationService personalInformationService, PasswordEncoder passwordEncoder) {
        if (!userRepository.existsByUsernameOrEmail(adminUsername, adminEmail)) {
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
            pi.setBirthDate(ZonedDateTime.now().minusYears(30));
            pi = personalInformationService.save(pi);

            Admin admin = new Admin();
            admin.setUsername(adminUsername);
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setPersonalInformation(pi);
            admin.setCreatedAt(OffsetDateTime.now());
            admin.setAccountStatus(AccountStatus.NORMAL);

            userRepository.save(admin);
            log.info("Registered default admin username={} id={}", admin.getUsername(), admin.getId());
        }
    }

    private void createRealtorIfNotFound(RemaxUserRepository userRepository, AddressService addressService,
                                         PersonalInformationService personalInformationService, PasswordEncoder passwordEncoder) {
        if (!userRepository.existsByUsernameOrEmail(realtorUsername, realtorEmail)) {
            log.info("Registering test realtor...");

            Address address = new Address();
            address.setStreet("Veveří 10");
            address.setCity("Brno");
            address.setPostalCode("602 00");
            address.setCountry("Česká republika");
            address.setRegion(AddressRegion.JIHOMORAVSKY);
            address = addressService.save(address);

            PersonalInformation pi = new PersonalInformation();
            pi.setAddress(address);
            pi.setFirstName("Petr");
            pi.setLastName("Makléř");
            pi.setPhoneNumber("+420 777 888 999");
            pi.setBirthDate(ZonedDateTime.now().minusYears(35));
            pi = personalInformationService.save(pi);

            Realtor realtor = new Realtor();
            realtor.setUsername(realtorUsername);
            realtor.setEmail(realtorEmail);
            realtor.setPassword(passwordEncoder.encode(realtorPassword));
            realtor.setPersonalInformation(pi);
            realtor.setCreatedAt(OffsetDateTime.now());
            realtor.setAccountStatus(AccountStatus.NORMAL);
            realtor.setLicenseNumber(123456);
            realtor.setAbout("Zkušený makléř se specializací na Jihomoravský kraj.");

            userRepository.save(realtor);
            log.info("Registered test realtor username={} id={}", realtor.getUsername(), realtor.getId());
        }
    }

    private void createClientIfNotFound(RemaxUserRepository userRepository, AddressService addressService,
                                        PersonalInformationService personalInformationService, PasswordEncoder passwordEncoder) {
        if (!userRepository.existsByUsernameOrEmail(clientUsername, clientEmail)) {
            log.info("Registering test client...");

            Address address = new Address();
            address.setStreet("Václavské náměstí 1");
            address.setCity("Praha");
            address.setPostalCode("110 00");
            address.setCountry("Česká republika");
            address.setRegion(AddressRegion.PRAHA);
            address = addressService.save(address);

            PersonalInformation pi = new PersonalInformation();
            pi.setAddress(address);
            pi.setFirstName("Alena");
            pi.setLastName("Zákazníková");
            pi.setPhoneNumber("+420 600 100 200");
            pi.setBirthDate(ZonedDateTime.now().minusYears(25));
            pi = personalInformationService.save(pi);

            Client client = new Client();
            client.setUsername(clientUsername);
            client.setEmail(clientEmail);
            client.setPassword(passwordEncoder.encode(clientPassword));
            client.setPersonalInformation(pi);
            client.setCreatedAt(OffsetDateTime.now());
            client.setAccountStatus(AccountStatus.NORMAL);

            userRepository.save(client);
            log.info("Registered test client username={} id={}", client.getUsername(), client.getId());
        }
    }
}
