package fei.upce.nnpro.remax.address.dto;

import fei.upce.nnpro.remax.address.entity.Address;
import fei.upce.nnpro.remax.realestates.entity.enums.AddressRegion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AddressMapperTest {
    private final AddressMapper mapper = new AddressMapper();

    @Test
    void toDto_nullEntity_returnsNull() {
        assertNull(mapper.toDto(null));
    }

    @Test
    void toDto_mapsAllFields() {
        Address entity = new Address();
        entity.setId(1L);
        entity.setStreet("Main St");
        entity.setCity("Prague");
        entity.setPostalCode("12345");
        entity.setCountry("CZ");
        entity.setFlatNumber("12A");
        entity.setRegion(AddressRegion.PRAHA);
        entity.setLatitude(50.1);
        entity.setLongitude(14.4);
        AddressDto dto = mapper.toDto(entity);
        assertNotNull(dto);
        assertEquals(entity.getId(), dto.getId());
        assertEquals(entity.getStreet(), dto.getStreet());
        assertEquals(entity.getCity(), dto.getCity());
        assertEquals(entity.getPostalCode(), dto.getPostalCode());
        assertEquals(entity.getCountry(), dto.getCountry());
        assertEquals(entity.getFlatNumber(), dto.getFlatNumber());
        assertEquals(entity.getRegion(), dto.getRegion());
        assertEquals(entity.getLatitude(), dto.getLatitude());
        assertEquals(entity.getLongitude(), dto.getLongitude());
    }

    @Test
    void toEntity_nullDto_returnsNull() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    void toEntity_mapsAllFields() {
        AddressDto dto = new AddressDto();
        dto.setId(2L);
        dto.setStreet("Side St");
        dto.setCity("Brno");
        dto.setPostalCode("54321");
        dto.setCountry("CZ");
        dto.setFlatNumber("34B");
        dto.setRegion(AddressRegion.JIHOMORAVSKY);
        dto.setLatitude(49.2);
        dto.setLongitude(16.6);
        Address entity = mapper.toEntity(dto);
        assertNotNull(entity);
        assertEquals(dto.getId(), entity.getId());
        assertEquals(dto.getStreet(), entity.getStreet());
        assertEquals(dto.getCity(), entity.getCity());
        assertEquals(dto.getPostalCode(), entity.getPostalCode());
        assertEquals(dto.getCountry(), entity.getCountry());
        assertEquals(dto.getFlatNumber(), entity.getFlatNumber());
        assertEquals(dto.getRegion(), entity.getRegion());
        assertEquals(dto.getLatitude(), entity.getLatitude());
        assertEquals(dto.getLongitude(), entity.getLongitude());
    }

    @Test
    void toEntity_dtoWithNullId_doesNotSetId() {
        AddressDto dto = new AddressDto();
        dto.setId(null);
        Address entity = mapper.toEntity(dto);
        assertNull(entity.getId());
    }
}
