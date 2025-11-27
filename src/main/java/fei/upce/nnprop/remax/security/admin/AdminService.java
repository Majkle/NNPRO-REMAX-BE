package fei.upce.nnprop.remax.security.admin;

import fei.upce.nnprop.remax.model.realestates.entity.Address;
import fei.upce.nnprop.remax.address.AddressService;
import fei.upce.nnprop.remax.model.users.*;
import fei.upce.nnprop.remax.model.users.enums.AccountStatus;
import fei.upce.nnprop.remax.security.auth.request.RegisterRequest;
import fei.upce.nnprop.remax.personalInformation.PersonalInformationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private final RemaxUserRepository userRepository;
    private final AddressService addressService;
    private final PersonalInformationService personalInformationService;
    private final PasswordEncoder passwordEncoder;

    public AdminService(RemaxUserRepository userRepository,
                        AddressService addressService,
                        PersonalInformationService personalInformationService,
                        PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.addressService = addressService;
        this.personalInformationService = personalInformationService;
        this.passwordEncoder = passwordEncoder;
    }

    public RemaxUser blockUser(String username, ZonedDateTime until) {
        Optional<RemaxUser> maybe = userRepository.findByUsername(username);
        if (maybe.isEmpty()) throw new IllegalArgumentException("User not found");
        RemaxUser user = maybe.get();
        user.setAccountStatus(AccountStatus.BLOCKED);
        user.setBlockedUntil(until);
        RemaxUser saved = userRepository.save(user);
        log.info("Admin blocked user {} until {}", username, until);
        return saved;
    }

    public RemaxUser unblockUser(String username) {
        Optional<RemaxUser> maybe = userRepository.findByUsername(username);
        if (maybe.isEmpty()) throw new IllegalArgumentException("User not found");
        RemaxUser user = maybe.get();
        user.setAccountStatus(AccountStatus.NORMAL);
        user.setBlockedUntil(null);
        user.setFailedLoginAttempts(0);
        RemaxUser saved = userRepository.save(user);
        log.info("Admin unblocked user {}", username);
        return saved;
    }

    public void deleteUser(String username) {
        Optional<RemaxUser> maybe = userRepository.findByUsername(username);
        if (maybe.isEmpty()) throw new IllegalArgumentException("User not found");
        userRepository.delete(maybe.get());
        log.info("Admin deleted user {}", username);
    }

    public Realtor createRealtor(CreateUserRequest req) {
        // ensure username/email unique
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            log.warn("Attempt to create realtor with existing username={}", req.getUsername());
            throw new IllegalArgumentException("Username exists");
        }
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            log.warn("Attempt to create realtor with existing email={}", req.getEmail());
            throw new IllegalArgumentException("Email exists");
        }

        Address address = addressService.createFrom(mapToRegisterRequest(req));
        PersonalInformation pi = personalInformationService.createFrom(mapToRegisterRequest(req), address);

        Realtor realtor = new Realtor();
        realtor.setUsername(req.getUsername());
        realtor.setEmail(req.getEmail());
        realtor.setPassword(passwordEncoder.encode(req.getPassword()));
        realtor.setCreatedAt(java.time.OffsetDateTime.now());
        realtor.setAccountStatus(AccountStatus.NORMAL);
        realtor.setPersonalInformation(pi);
        realtor.setLicenseNumber(req.getLicenseNumber());
        realtor.setAbout(req.getAbout());

        Realtor saved = userRepository.save(realtor);
        log.info("Admin created realtor username={} id={}", saved.getUsername(), saved.getId());
        return saved;
    }

    public Admin createAdmin(CreateUserRequest req) {
        // ensure username/email unique
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            log.warn("Attempt to create admin with existing username={}", req.getUsername());
            throw new IllegalArgumentException("Username exists");
        }
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            log.warn("Attempt to create admin with existing email={}", req.getEmail());
            throw new IllegalArgumentException("Email exists");
        }

        Address address = addressService.createFrom(mapToRegisterRequest(req));
        PersonalInformation pi = personalInformationService.createFrom(mapToRegisterRequest(req), address);

        Admin admin = new Admin();
        admin.setUsername(req.getUsername());
        admin.setEmail(req.getEmail());
        admin.setPassword(passwordEncoder.encode(req.getPassword()));
        admin.setCreatedAt(java.time.OffsetDateTime.now());
        admin.setAccountStatus(AccountStatus.NORMAL);
        admin.setPersonalInformation(pi);

        Admin saved = userRepository.save(admin);
        log.info("Admin created realtor username={} id={}", saved.getUsername(), saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<RemaxUserResponse> listAllUsers() {
        return userRepository.findAll()
                .stream()
                .map((user) -> {
                    user.setPassword("********");
                    return RemaxUserResponse.createFrom(user);
                })
                .collect(Collectors.toList());
    }

    private RegisterRequest mapToRegisterRequest(CreateUserRequest r) {
        RegisterRequest reg = new RegisterRequest();
        reg.setUsername(r.getUsername());
        reg.setEmail(r.getEmail());
        reg.setPassword(r.getPassword());
        reg.setFirstName(r.getFirstName());
        reg.setLastName(r.getLastName());
        reg.setPhoneNumber(r.getPhoneNumber());
        reg.setBirthDate(r.getBirthDate());
        reg.setStreet(r.getStreet());
        reg.setCity(r.getCity());
        reg.setPostalCode(r.getPostalCode());
        reg.setCountry(r.getCountry());
        reg.setFlatNumber(r.getFlatNumber());
        reg.setRegion(r.getRegion());
        return reg;
    }
}
