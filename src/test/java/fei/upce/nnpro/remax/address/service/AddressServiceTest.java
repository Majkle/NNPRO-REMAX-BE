package fei.upce.nnpro.remax.address.service;

import fei.upce.nnpro.remax.address.entity.Address;
import fei.upce.nnpro.remax.address.repository.AddressRepository;
import fei.upce.nnpro.remax.security.auth.request.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    private AddressService sut;

    @BeforeEach
    void setUp() {
        sut = new AddressService(addressRepository);
    }

    @Test
    void save_shouldDelegateToRepository_andReturnSaved() {
        Address in = new Address();
        in.setCity("City");
        in.setLatitude(50.0);
        in.setLongitude(14.0);

        Address out = new Address();
        out.setId(1L);
        out.setCity("City");
        out.setLatitude(50.0);
        out.setLongitude(14.0);

        when(addressRepository.save(in)).thenReturn(out);

        Address saved = sut.save(in);

        assertThat(saved).isSameAs(out);
        assertThat(saved.getLatitude()).isEqualTo(50.0);
        verify(addressRepository).save(in);
    }

    @Test
    void update_shouldApplyFieldsAndSave() {
        Address existing = new Address();
        existing.setId(10L);
        existing.setCity("Old");
        existing.setLatitude(10.0);

        Address newer = new Address();
        newer.setCity("NewCity");
        newer.setStreet("Street A");
        newer.setPostalCode("11111");
        newer.setCountry("CZ");
        newer.setFlatNumber("5");
        // Update GPS
        newer.setLatitude(50.0755);
        newer.setLongitude(14.4378);

        when(addressRepository.save(existing)).thenReturn(existing);

        sut.update(newer, existing);

        ArgumentCaptor<Address> cap = ArgumentCaptor.forClass(Address.class);
        verify(addressRepository).save(cap.capture());
        Address saved = cap.getValue();

        assertThat(saved.getCity()).isEqualTo("NewCity");
        assertThat(saved.getStreet()).isEqualTo("Street A");
        // Check GPS update
        assertThat(saved.getLatitude()).isEqualTo(50.0755);
        assertThat(saved.getLongitude()).isEqualTo(14.4378);
    }

    @Test
    void createFrom_shouldParseRegionOrThrow() {
        RegisterRequest r = new RegisterRequest();
        r.setCity("C");
        r.setStreet("S");
        r.setPostalCode("PC");
        r.setCountry("CZ");
        r.setFlatNumber("1");
        r.setRegion("PRAHA");

        Address saved = new Address();
        saved.setId(2L);
        when(addressRepository.save(any())).thenReturn(saved);

        Address result = sut.createFrom(r);
        assertThat(result).isSameAs(saved);

        // invalid region
        r.setRegion("INVALID_REGION");
        assertThatThrownBy(() -> sut.createFrom(r)).isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid address region");
    }
}

