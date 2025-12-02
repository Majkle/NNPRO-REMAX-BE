package fei.upce.nnpro.remax.address;

import fei.upce.nnpro.remax.model.realestates.entity.Address;
import fei.upce.nnpro.remax.model.realestates.enums.AddressRegion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AddressService addressService;

    @Test
    void save_ShouldCallRepositorySave() {
        // Arrange
        Address address = new Address();
        address.setCity("Prague");
        when(addressRepository.save(any(Address.class))).thenAnswer(invocation -> invocation.getArgument(0));
        // Act
        addressService.save(address);

        // Assert
        verify(addressRepository).save(address);
    }

    @Test
    void update_ShouldUpdateFieldsAndSave() {
        // Arrange
        Address existingAddress = new Address();
        existingAddress.setId(1L);
        existingAddress.setCity("Old City");
        existingAddress.setStreet("Old Street");
        existingAddress.setRegion(AddressRegion.JIHOCESKY);

        Address newAddressData = new Address();
        newAddressData.setCity("New City");
        newAddressData.setStreet("New Street");
        newAddressData.setPostalCode("12345");
        newAddressData.setCountry("Czechia");
        newAddressData.setFlatNumber("10");
        newAddressData.setRegion(AddressRegion.PRAHA);

        // Act
        addressService.update(newAddressData, existingAddress);

        // Assert
        ArgumentCaptor<Address> addressCaptor = ArgumentCaptor.forClass(Address.class);
        verify(addressRepository).save(addressCaptor.capture());

        Address capturedAddress = addressCaptor.getValue();

        // ID should remain the same
        assertThat(capturedAddress.getId()).isEqualTo(1L);

        // Fields should be updated
        assertThat(capturedAddress.getCity()).isEqualTo("New City");
        assertThat(capturedAddress.getStreet()).isEqualTo("New Street");
        assertThat(capturedAddress.getPostalCode()).isEqualTo("12345");
        assertThat(capturedAddress.getCountry()).isEqualTo("Czechia");
        assertThat(capturedAddress.getFlatNumber()).isEqualTo("10");
        assertThat(capturedAddress.getRegion()).isEqualTo(AddressRegion.PRAHA);
    }
}