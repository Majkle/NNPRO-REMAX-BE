package fei.upce.nnprop.remax.address;

import fei.upce.nnprop.remax.model.realestates.Address;
import fei.upce.nnprop.remax.model.realestates.AddressRepository;
import fei.upce.nnprop.remax.model.realestates.enums.AddressRegion;
import fei.upce.nnprop.remax.security.auth.request.RegisterRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AddressService {

    private static final Logger log = LoggerFactory.getLogger(AddressService.class);

    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public Address save(Address address) {
        Address saved = addressRepository.save(address);
        log.info("Saved address id={}", saved.getId());
        return saved;
    }

    // Initialize Address from RegisterRequest and persist it
    public Address createFrom(RegisterRequest request) {
        Address address = new Address();
        address.setStreet(request.getStreet());
        address.setCity(request.getCity());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        address.setFlatNumber(request.getFlatNumber());
        try {
            address.setRegion(AddressRegion.valueOf(request.getRegion().toUpperCase()));
        } catch (IllegalArgumentException e) {
            log.warn("Invalid address region: {}", request.getRegion());
            throw new IllegalArgumentException("Invalid address region: " + request.getRegion());
        }
        Address saved = save(address);
        log.info("Created address id={} for street={}, city={}", saved.getId(), saved.getStreet(), saved.getCity());
        return saved;
    }
}
