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
import fei.upce.nnpro.remax.realestates.entity.enums.AddressRegion;
import fei.upce.nnpro.remax.security.auth.request.RegisterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
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

    public RemaxUser updateUser(String username, UpdateUserRequest req) {
        RemaxUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!Objects.equals(user.getEmail(), req.getEmail()) && userRepository.findByEmail(req.getEmail()).isPresent()) {
            log.warn("Attempt to update a user with a new but already existing email={}", req.getEmail());
            throw new IllegalArgumentException("Email exists");
        }

        // update or create address
        Address address = null;
        if (user.getPersonalInformation() != null) {
            address = user.getPersonalInformation().getAddress();
        }
        if (address == null) address = new Address();

        address.setStreet(req.getStreet());
        address.setCity(req.getCity());
        address.setPostalCode(req.getPostalCode());
        address.setCountry(req.getCountry());
        address.setFlatNumber(req.getFlatNumber());
        try {
            address.setRegion(AddressRegion.valueOf(req.getRegion().toUpperCase()));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid region provided for username={}: {}", username, req.getRegion());
            throw e;
        }
        Address savedAddress = addressService.save(address);
        log.info("Saved address id={} for username={}", savedAddress.getId(), username);

        PersonalInformation pi = user.getPersonalInformation();
        if (pi == null) pi = new PersonalInformation();
        pi.setDegree(req.getDegree());
        pi.setFirstName(req.getFirstName());
        pi.setLastName(req.getLastName());
        pi.setPhoneNumber(req.getPhoneNumber());
        try {
            pi.setBirthDate(ZonedDateTime.parse(req.getBirthDate()));
        } catch (Exception ex) {
            log.warn("Invalid birthDate for username={}: {}", username, req.getBirthDate());
            throw new IllegalArgumentException("Invalid birthDate: " + req.getBirthDate());
        }
        pi.setAddress(savedAddress);
        PersonalInformation savedPi = personalInformationService.save(pi);
        log.info("Saved personalInformation id={} for username={}", savedPi.getId(), username);

        user.setEmail(req.getEmail());
        user.setPersonalInformation(savedPi);
        RemaxUser savedUser = userRepository.save(user);
        log.info("Updated profile for username={}", username);
        return savedUser;
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
        reg.setDegree(r.getDegree());
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
