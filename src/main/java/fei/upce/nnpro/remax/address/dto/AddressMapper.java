package fei.upce.nnpro.remax.address.dto;

import fei.upce.nnpro.remax.address.entity.Address;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public AddressDto toDto(Address entity) {
        if (entity == null) return null;

        AddressDto dto = new AddressDto();
        dto.setId(entity.getId());
        dto.setStreet(entity.getStreet());
        dto.setCity(entity.getCity());
        dto.setPostalCode(entity.getPostalCode());
        dto.setCountry(entity.getCountry());
        dto.setFlatNumber(entity.getFlatNumber());
        dto.setRegion(entity.getRegion());
        dto.setLatitude(entity.getLatitude());
        dto.setLongitude(entity.getLongitude());
        return dto;
    }

    public Address toEntity(AddressDto dto) {
        if (dto == null) return null;

        Address entity = new Address();
        if (dto.getId() != null) {
            entity.setId(dto.getId());
        }
        entity.setStreet(dto.getStreet());
        entity.setCity(dto.getCity());
        entity.setPostalCode(dto.getPostalCode());
        entity.setCountry(dto.getCountry());
        entity.setFlatNumber(dto.getFlatNumber());
        entity.setRegion(dto.getRegion());
        entity.setLatitude(dto.getLatitude());
        entity.setLongitude(dto.getLongitude());
        return entity;
    }
}