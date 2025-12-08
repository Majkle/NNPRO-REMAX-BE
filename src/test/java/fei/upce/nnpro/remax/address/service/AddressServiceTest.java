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
        Address out = new Address();
        out.setId(1L);
        out.setCity("City");
        when(addressRepository.save(in)).thenReturn(out);

        Address saved = sut.save(in);

        assertThat(saved).isSameAs(out);
        verify(addressRepository).save(in);
    }

    @Test
    void update_shouldApplyFieldsAndSave() {
        Address existing = new Address();
        existing.setId(10L);
        existing.setCity("Old");

        Address newer = new Address();
        newer.setCity("NewCity");
        newer.setStreet("Street A");
        newer.setPostalCode("11111");
        newer.setCountry("CZ");
        newer.setFlatNumber("5");

        when(addressRepository.save(existing)).thenReturn(existing);

        sut.update(newer, existing);

        ArgumentCaptor<Address> cap = ArgumentCaptor.forClass(Address.class);
        verify(addressRepository).save(cap.capture());
        Address saved = cap.getValue();
        assertThat(saved.getCity()).isEqualTo("NewCity");
        assertThat(saved.getStreet()).isEqualTo("Street A");
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

