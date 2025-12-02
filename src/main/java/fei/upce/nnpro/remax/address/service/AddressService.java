package fei.upce.nnpro.remax.address.service;

import fei.upce.nnpro.remax.address.entity.Address;
import fei.upce.nnpro.remax.address.repository.AddressRepository;
import fei.upce.nnpro.remax.realestates.entity.enums.AddressRegion;
import fei.upce.nnpro.remax.security.auth.request.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final AddressRepository addressRepository;
    private static final Logger log = LoggerFactory.getLogger(AddressService.class);

    public Address save(Address address) {
        Address saved = addressRepository.save(address);
        log.info("Saved address id={}", saved.getId());
        return saved;
    }

    public void update(Address newAddrData, Address existingAddr) {
        existingAddr.setCity(newAddrData.getCity());
        existingAddr.setStreet(newAddrData.getStreet());
        existingAddr.setPostalCode(newAddrData.getPostalCode());
        existingAddr.setCountry(newAddrData.getCountry());
        existingAddr.setFlatNumber(newAddrData.getFlatNumber());
        existingAddr.setRegion(newAddrData.getRegion());

        addressRepository.save(existingAddr);
    }

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
