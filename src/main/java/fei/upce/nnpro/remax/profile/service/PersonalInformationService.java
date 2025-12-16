package fei.upce.nnpro.remax.profile.service;

import fei.upce.nnpro.remax.address.entity.Address;
import fei.upce.nnpro.remax.images.service.ImageService;
import fei.upce.nnpro.remax.profile.entity.PersonalInformation;
import fei.upce.nnpro.remax.profile.repository.PersonalInformationRepository;
import fei.upce.nnpro.remax.security.auth.request.RegisterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

@Service
public class PersonalInformationService {

    private static final Logger log = LoggerFactory.getLogger(PersonalInformationService.class);

    private final PersonalInformationRepository personalInformationRepository;
    private final ImageService imageService;

    public PersonalInformationService(PersonalInformationRepository personalInformationRepository, ImageService imageService) {
        this.personalInformationRepository = personalInformationRepository;
        this.imageService = imageService;
    }

    public PersonalInformation save(PersonalInformation pi) {
        PersonalInformation saved = personalInformationRepository.save(pi);
        log.info("Saved PersonalInformation id={}", saved.getId());
        return saved;
    }

    public PersonalInformation createFrom(RegisterRequest request, Address address) {
        PersonalInformation pi = new PersonalInformation();
        pi.setFirstName(request.getFirstName());
        pi.setLastName(request.getLastName());
        pi.setPhoneNumber(request.getPhoneNumber());

        if (request.getImage() != null)
            pi.setImage(imageService.getImageEntity(request.getImage()));

        try {
            pi.setBirthDate(ZonedDateTime.parse(request.getBirthDate()));
        } catch (DateTimeParseException e) {
            log.warn("Invalid birthDate format: {}", request.getBirthDate());
            throw new IllegalArgumentException("Invalid birthDate format. Use ISO-8601 date-time");
        }
        pi.setAddress(address);
        PersonalInformation saved = save(pi);
        log.info("Created PersonalInformation id={}", saved.getId());
        return saved;
    }
}
