package fei.upce.nnpro.remax.profile.service;

import fei.upce.nnpro.remax.address.entity.Address;
import fei.upce.nnpro.remax.address.service.AddressService;
import fei.upce.nnpro.remax.profile.dto.ProfileUpdateRequest;
import fei.upce.nnpro.remax.profile.entity.Client;
import fei.upce.nnpro.remax.profile.entity.PersonalInformation;
import fei.upce.nnpro.remax.profile.entity.RemaxUser;
import fei.upce.nnpro.remax.profile.repository.RemaxUserRepository;
import fei.upce.nnpro.remax.realestates.entity.enums.AddressRegion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    @Mock
    private RemaxUserRepository userRepository;

    @Mock
    private AddressService addressService;

    @Mock
    private PersonalInformationService personalInformationService;

    private ProfileService sut;

    @BeforeEach
    void setUp() {
        sut = new ProfileService(userRepository, addressService, personalInformationService);
    }

    // --------------------------------------------------------------------------------------
    // GET PROFILE TESTS
    // --------------------------------------------------------------------------------------

    @Test
    @DisplayName("getProfile: Should return user when found")
    void getProfile_Success() {
        // Arrange
        String username = "testuser";
        RemaxUser user = new Client();
        user.setUsername(username);
        user.setId(1L);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        Optional<RemaxUser> result = sut.getProfile(username);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo(username);
        verify(userRepository).findByUsername(username);
    }

    @Test
    @DisplayName("getProfile: Should return empty when user not found")
    void getProfile_UserNotFound() {
        // Arrange
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act
        Optional<RemaxUser> result = sut.getProfile(username);

        // Assert
        assertThat(result).isEmpty();
        verify(userRepository).findByUsername(username);
    }

    // --------------------------------------------------------------------------------------
    // UPDATE PROFILE TESTS
    // --------------------------------------------------------------------------------------

    @Test
    @DisplayName("updateProfile: Should update profile with new address and personal information")
    void updateProfile_Success_NewAddressAndPersonalInfo() {
        // Arrange
        String username = "testuser";
        RemaxUser user = new Client();
        user.setUsername(username);
        user.setId(1L);
        user.setPersonalInformation(null); // No existing PI

        ProfileUpdateRequest request = createValidProfileUpdateRequest();

        Address savedAddress = new Address();
        savedAddress.setId(10L);
        savedAddress.setStreet(request.getStreet());
        savedAddress.setCity(request.getCity());

        PersonalInformation savedPi = new PersonalInformation();
        savedPi.setId(20L);
        savedPi.setFirstName(request.getFirstName());
        savedPi.setLastName(request.getLastName());
        savedPi.setAddress(savedAddress);

        RemaxUser savedUser = new Client();
        savedUser.setId(1L);
        savedUser.setPersonalInformation(savedPi);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(addressService.save(any(Address.class))).thenReturn(savedAddress);
        when(personalInformationService.save(any(PersonalInformation.class))).thenReturn(savedPi);
        when(userRepository.save(user)).thenReturn(savedUser);

        // Act
        RemaxUser result = sut.updateProfile(username, request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getPersonalInformation()).isEqualTo(savedPi);

        // Verify address was saved with correct data
        ArgumentCaptor<Address> addressCaptor = ArgumentCaptor.forClass(Address.class);
        verify(addressService).save(addressCaptor.capture());
        Address capturedAddress = addressCaptor.getValue();
        assertThat(capturedAddress.getStreet()).isEqualTo(request.getStreet());
        assertThat(capturedAddress.getCity()).isEqualTo(request.getCity());
        assertThat(capturedAddress.getPostalCode()).isEqualTo(request.getPostalCode());
        assertThat(capturedAddress.getCountry()).isEqualTo(request.getCountry());
        assertThat(capturedAddress.getFlatNumber()).isEqualTo(request.getFlatNumber());
        assertThat(capturedAddress.getRegion()).isEqualTo(AddressRegion.PRAHA);

        // Verify personal information was saved with correct data
        ArgumentCaptor<PersonalInformation> piCaptor = ArgumentCaptor.forClass(PersonalInformation.class);
        verify(personalInformationService).save(piCaptor.capture());
        PersonalInformation capturedPi = piCaptor.getValue();
        assertThat(capturedPi.getFirstName()).isEqualTo(request.getFirstName());
        assertThat(capturedPi.getLastName()).isEqualTo(request.getLastName());
        assertThat(capturedPi.getPhoneNumber()).isEqualTo(request.getPhoneNumber());
        assertThat(capturedPi.getBirthDate()).isNotNull();
        assertThat(capturedPi.getAddress()).isEqualTo(savedAddress);

        verify(userRepository).save(user);
    }

    @Test
    @DisplayName("updateProfile: Should update profile with existing address")
    void updateProfile_Success_ExistingAddress() {
        // Arrange
        String username = "testuser";

        Address existingAddress = new Address();
        existingAddress.setId(5L);
        existingAddress.setStreet("Old Street");
        existingAddress.setCity("Old City");

        PersonalInformation existingPi = new PersonalInformation();
        existingPi.setId(15L);
        existingPi.setAddress(existingAddress);

        RemaxUser user = new Client();
        user.setUsername(username);
        user.setId(1L);
        user.setPersonalInformation(existingPi);

        ProfileUpdateRequest request = createValidProfileUpdateRequest();

        Address savedAddress = new Address();
        savedAddress.setId(5L);
        savedAddress.setStreet(request.getStreet());

        PersonalInformation savedPi = new PersonalInformation();
        savedPi.setId(15L);

        RemaxUser savedUser = new Client();
        savedUser.setId(1L);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(addressService.save(any(Address.class))).thenReturn(savedAddress);
        when(personalInformationService.save(any(PersonalInformation.class))).thenReturn(savedPi);
        when(userRepository.save(user)).thenReturn(savedUser);

        // Act
        RemaxUser result = sut.updateProfile(username, request);

        // Assert
        assertThat(result).isNotNull();

        // Verify the existing address was updated
        ArgumentCaptor<Address> addressCaptor = ArgumentCaptor.forClass(Address.class);
        verify(addressService).save(addressCaptor.capture());
        Address capturedAddress = addressCaptor.getValue();
        assertThat(capturedAddress).isSameAs(existingAddress);
        assertThat(capturedAddress.getStreet()).isEqualTo(request.getStreet());
    }

    @Test
    @DisplayName("updateProfile: Should update profile with existing personal information but no address")
    void updateProfile_Success_ExistingPiNoAddress() {
        // Arrange
        String username = "testuser";

        PersonalInformation existingPi = new PersonalInformation();
        existingPi.setId(15L);
        existingPi.setAddress(null); // No address set

        RemaxUser user = new Client();
        user.setUsername(username);
        user.setId(1L);
        user.setPersonalInformation(existingPi);

        ProfileUpdateRequest request = createValidProfileUpdateRequest();

        Address savedAddress = new Address();
        savedAddress.setId(10L);

        PersonalInformation savedPi = new PersonalInformation();
        savedPi.setId(15L);

        RemaxUser savedUser = new Client();
        savedUser.setId(1L);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(addressService.save(any(Address.class))).thenReturn(savedAddress);
        when(personalInformationService.save(any(PersonalInformation.class))).thenReturn(savedPi);
        when(userRepository.save(user)).thenReturn(savedUser);

        // Act
        RemaxUser result = sut.updateProfile(username, request);

        // Assert
        assertThat(result).isNotNull();
        verify(addressService).save(any(Address.class));
        verify(personalInformationService).save(existingPi);
    }

    @Test
    @DisplayName("updateProfile: Should throw exception when user not found")
    void updateProfile_Fail_UserNotFound() {
        // Arrange
        String username = "nonexistent";
        ProfileUpdateRequest request = createValidProfileUpdateRequest();

        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> sut.updateProfile(username, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");

        verify(addressService, never()).save(any());
        verify(personalInformationService, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateProfile: Should throw exception when region is invalid")
    void updateProfile_Fail_InvalidRegion() {
        // Arrange
        String username = "testuser";
        RemaxUser user = new Client();
        user.setUsername(username);
        user.setId(1L);

        ProfileUpdateRequest request = createValidProfileUpdateRequest();
        request.setRegion("INVALID_REGION");

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Act & Assert
        assertThatThrownBy(() -> sut.updateProfile(username, request))
                .isInstanceOf(IllegalArgumentException.class);

        verify(addressService, never()).save(any());
        verify(personalInformationService, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateProfile: Should throw exception when region is lowercase but valid")
    void updateProfile_Success_LowercaseRegion() {
        // Arrange
        String username = "testuser";
        RemaxUser user = new Client();
        user.setUsername(username);
        user.setId(1L);

        ProfileUpdateRequest request = createValidProfileUpdateRequest();
        request.setRegion("praha"); // lowercase

        Address savedAddress = new Address();
        savedAddress.setId(10L);

        PersonalInformation savedPi = new PersonalInformation();
        savedPi.setId(20L);

        RemaxUser savedUser = new Client();
        savedUser.setId(1L);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(addressService.save(any(Address.class))).thenReturn(savedAddress);
        when(personalInformationService.save(any(PersonalInformation.class))).thenReturn(savedPi);
        when(userRepository.save(user)).thenReturn(savedUser);

        // Act
        RemaxUser result = sut.updateProfile(username, request);

        // Assert
        assertThat(result).isNotNull();

        // Verify region was converted to uppercase
        ArgumentCaptor<Address> addressCaptor = ArgumentCaptor.forClass(Address.class);
        verify(addressService).save(addressCaptor.capture());
        assertThat(addressCaptor.getValue().getRegion()).isEqualTo(AddressRegion.PRAHA);
    }

    @Test
    @DisplayName("updateProfile: Should throw exception when birthDate is invalid")
    void updateProfile_Fail_InvalidBirthDate() {
        // Arrange
        String username = "testuser";
        RemaxUser user = new Client();
        user.setUsername(username);
        user.setId(1L);

        ProfileUpdateRequest request = createValidProfileUpdateRequest();
        request.setBirthDate("not-a-valid-date");

        Address savedAddress = new Address();
        savedAddress.setId(10L);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(addressService.save(any(Address.class))).thenReturn(savedAddress);

        // Act & Assert
        assertThatThrownBy(() -> sut.updateProfile(username, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid birthDate");

        verify(personalInformationService, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateProfile: Should throw exception when birthDate is null")
    void updateProfile_Fail_NullBirthDate() {
        // Arrange
        String username = "testuser";
        RemaxUser user = new Client();
        user.setUsername(username);
        user.setId(1L);

        ProfileUpdateRequest request = createValidProfileUpdateRequest();
        request.setBirthDate(null);

        Address savedAddress = new Address();
        savedAddress.setId(10L);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(addressService.save(any(Address.class))).thenReturn(savedAddress);

        // Act & Assert
        assertThatThrownBy(() -> sut.updateProfile(username, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid birthDate");

        verify(personalInformationService, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateProfile: Should handle empty string birthDate")
    void updateProfile_Fail_EmptyBirthDate() {
        // Arrange
        String username = "testuser";
        RemaxUser user = new Client();
        user.setUsername(username);
        user.setId(1L);

        ProfileUpdateRequest request = createValidProfileUpdateRequest();
        request.setBirthDate("");

        Address savedAddress = new Address();
        savedAddress.setId(10L);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
        when(addressService.save(any(Address.class))).thenReturn(savedAddress);

        // Act & Assert
        assertThatThrownBy(() -> sut.updateProfile(username, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid birthDate");

        verify(personalInformationService, never()).save(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("updateProfile: Should handle all different valid regions")
    void updateProfile_Success_AllValidRegions() {
        // Test a few different valid regions
        String[] validRegions = {"PRAHA", "STREDOCESKY", "JIHOCESKY", "MORAVSKOSLEZSKY"};

        for (String region : validRegions) {
            // Arrange
            String username = "testuser";
            RemaxUser user = new Client();
            user.setUsername(username);
            user.setId(1L);

            ProfileUpdateRequest request = createValidProfileUpdateRequest();
            request.setRegion(region);

            Address savedAddress = new Address();
            savedAddress.setId(10L);

            PersonalInformation savedPi = new PersonalInformation();
            savedPi.setId(20L);

            RemaxUser savedUser = new Client();
            savedUser.setId(1L);

            when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));
            when(addressService.save(any(Address.class))).thenReturn(savedAddress);
            when(personalInformationService.save(any(PersonalInformation.class))).thenReturn(savedPi);
            when(userRepository.save(user)).thenReturn(savedUser);

            // Act
            RemaxUser result = sut.updateProfile(username, request);

            // Assert
            assertThat(result).isNotNull();

            // Verify region was set correctly
            ArgumentCaptor<Address> addressCaptor = ArgumentCaptor.forClass(Address.class);
            verify(addressService, atLeastOnce()).save(addressCaptor.capture());
            assertThat(addressCaptor.getValue().getRegion()).isEqualTo(AddressRegion.valueOf(region));

            // Reset mocks for next iteration
            reset(userRepository, addressService, personalInformationService);
        }
    }

    // --------------------------------------------------------------------------------------
    // DELETE PROFILE TESTS
    // --------------------------------------------------------------------------------------

    @Test
    @DisplayName("deleteProfile: Should delete user when found")
    void deleteProfile_Success() {
        // Arrange
        String username = "testuser";
        RemaxUser user = new Client();
        user.setUsername(username);
        user.setId(1L);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        sut.deleteProfile(username);

        // Assert
        verify(userRepository).findByUsername(username);
        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("deleteProfile: Should throw exception when user not found")
    void deleteProfile_Fail_UserNotFound() {
        // Arrange
        String username = "nonexistent";
        when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> sut.deleteProfile(username))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");

        verify(userRepository).findByUsername(username);
        verify(userRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteProfile: Should delete user with complete profile")
    void deleteProfile_Success_WithCompleteProfile() {
        // Arrange
        String username = "testuser";

        Address address = new Address();
        address.setId(10L);

        PersonalInformation pi = new PersonalInformation();
        pi.setId(20L);
        pi.setAddress(address);

        RemaxUser user = new Client();
        user.setUsername(username);
        user.setId(1L);
        user.setPersonalInformation(pi);

        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // Act
        sut.deleteProfile(username);

        // Assert
        verify(userRepository).delete(user);
    }

    // --------------------------------------------------------------------------------------
    // HELPER METHODS
    // --------------------------------------------------------------------------------------

    private ProfileUpdateRequest createValidProfileUpdateRequest() {
        ProfileUpdateRequest request = new ProfileUpdateRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPhoneNumber("+420123456789");
        request.setBirthDate(ZonedDateTime.now().minusYears(30).toString());
        request.setStreet("Main Street 123");
        request.setCity("Prague");
        request.setPostalCode("11000");
        request.setCountry("Czech Republic");
        request.setFlatNumber("4A");
        request.setRegion("PRAHA");
        return request;
    }
}
