package fei.upce.nnprop.remax.personalInformation;

import fei.upce.nnprop.remax.model.realestates.entity.Address;
import fei.upce.nnprop.remax.model.users.PersonalInformation;
import fei.upce.nnprop.remax.model.users.PersonalInformationRepository;
import fei.upce.nnprop.remax.security.auth.request.RegisterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

@Service
public class PersonalInformationService {

    private static final Logger log = LoggerFactory.getLogger(PersonalInformationService.class);

    private final PersonalInformationRepository personalInformationRepository;

    public PersonalInformationService(PersonalInformationRepository personalInformationRepository) {
        this.personalInformationRepository = personalInformationRepository;
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
