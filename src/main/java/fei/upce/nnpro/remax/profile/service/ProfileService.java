package fei.upce.nnpro.remax.profile.service;

import fei.upce.nnpro.remax.address.service.AddressService;
import fei.upce.nnpro.remax.address.entity.Address;
import fei.upce.nnpro.remax.realestates.entity.enums.AddressRegion;
import fei.upce.nnpro.remax.profile.entity.PersonalInformation;
import fei.upce.nnpro.remax.profile.entity.RemaxUser;
import fei.upce.nnpro.remax.profile.repository.RemaxUserRepository;
import fei.upce.nnpro.remax.profile.dto.ProfileUpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.Optional;

@Service
public class ProfileService {

    private static final Logger log = LoggerFactory.getLogger(ProfileService.class);

    private final RemaxUserRepository userRepository;
    private final AddressService addressService;
    private final PersonalInformationService personalInformationService;

    public ProfileService(RemaxUserRepository userRepository,
                          AddressService addressService,
                          PersonalInformationService personalInformationService) {
        this.userRepository = userRepository;
        this.addressService = addressService;
        this.personalInformationService = personalInformationService;
    }

    public Optional<RemaxUser> getProfile(String username) {
        log.info("Fetching profile for username={}", username);
        return userRepository.findByUsername(username);
    }

    public RemaxUser updateProfile(String username, ProfileUpdateRequest request) {
        log.info("Updating profile for username={}", username);
        RemaxUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // update or create address
        Address address = null;
        if (user.getPersonalInformation() != null) {
            address = user.getPersonalInformation().getAddress();
        }
        if (address == null) address = new Address();

        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setFlatNumber(request.getFlatNumber());
        try {
            address.setRegion(AddressRegion.valueOf(request.getRegion().toUpperCase()));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid region provided for username={}: {}", username, request.getRegion());
            throw e;
        }
        Address savedAddress = addressService.save(address);
        log.info("Saved address id={} for username={}", savedAddress.getId(), username);

        PersonalInformation pi = user.getPersonalInformation();
        if (pi == null) pi = new PersonalInformation();
        pi.setFirstName(request.getFirstName());
        pi.setLastName(request.getLastName());
        pi.setPhoneNumber(request.getPhoneNumber());
        try {
            pi.setBirthDate(ZonedDateTime.parse(request.getBirthDate()));
        } catch (Exception ex) {
            log.warn("Invalid birthDate for username={}: {}", username, request.getBirthDate());
            throw new IllegalArgumentException("Invalid birthDate: " + request.getBirthDate());
        }
        pi.setAddress(savedAddress);
        PersonalInformation savedPi = personalInformationService.save(pi);
        log.info("Saved personalInformation id={} for username={}", savedPi.getId(), username);

        user.setPersonalInformation(savedPi);
        RemaxUser savedUser = userRepository.save(user);
        log.info("Updated profile for username={}", username);
        return savedUser;
    }

    public void deleteProfile(String username) {
        log.info("Deleting profile for username={}", username);
        RemaxUser user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found"));
        userRepository.delete(user);
        log.info("Deleted profile for username={}", username);
    }
}

