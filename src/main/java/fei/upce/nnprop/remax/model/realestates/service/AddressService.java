package fei.upce.nnprop.remax.model.realestates.service;

import fei.upce.nnprop.remax.model.realestates.dto.RealEstateDto;
import fei.upce.nnprop.remax.model.realestates.entity.Address;
import fei.upce.nnprop.remax.model.realestates.repository.AddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final AddressRepository addressRepository;

    public void save(Address address) {
        addressRepository.save(address);
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
}
