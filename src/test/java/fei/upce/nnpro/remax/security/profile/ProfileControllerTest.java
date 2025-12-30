package fei.upce.nnpro.remax.security.profile;

import fei.upce.nnpro.remax.address.entity.Address;
import fei.upce.nnpro.remax.profile.controller.ProfileController;
import fei.upce.nnpro.remax.profile.dto.ProfileUpdateRequest;
import fei.upce.nnpro.remax.profile.dto.RemaxUserResponse;
import fei.upce.nnpro.remax.profile.entity.PersonalInformation;
import fei.upce.nnpro.remax.profile.entity.RemaxUser;
import fei.upce.nnpro.remax.profile.service.ProfileService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProfileControllerTest {

    @Test
    void updateProfileHappyPath() {
        ProfileService profileService = Mockito.mock(ProfileService.class);

        ProfileController controller = new ProfileController(profileService);

        RemaxUser user = new RemaxUser() {};
        user.setUsername("testuser");
        PersonalInformation pi = new PersonalInformation();
        Address addr = new Address();
        addr.setId(1L);
        pi.setAddress(addr);
        user.setPersonalInformation(pi);

        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.isAuthenticated()).thenReturn(true);
        Mockito.when(auth.getName()).thenReturn("testuser");

        ProfileUpdateRequest req = new ProfileUpdateRequest();
        req.setFirstName("F");
        req.setLastName("L");
        req.setPhoneNumber("123");
        req.setBirthDate(ZonedDateTime.now().toString());
        req.setStreet("Street");
        req.setCity("City");
        req.setPostalCode("PC");
        req.setCountry("CZ");
        req.setRegion("PRAHA");

        // stub service to return updated user
        PersonalInformation updatedPi = new PersonalInformation();
        updatedPi.setFirstName("F");
        user.setPersonalInformation(updatedPi);
        Mockito.when(profileService.updateProfile(Mockito.eq("testuser"), Mockito.any(ProfileUpdateRequest.class))).thenReturn(user);

        ResponseEntity<?> resp = controller.updateProfile(auth, req);
        assertEquals(200, resp.getStatusCode().value());
        RemaxUserResponse updated = (RemaxUserResponse) resp.getBody();
        assertNotNull(updated);
        assertNotNull(updated.getPersonalInformation());
        assertEquals("F", updated.getPersonalInformation().getFirstName());

        Mockito.verify(profileService).updateProfile(Mockito.eq("testuser"), Mockito.any(ProfileUpdateRequest.class));
    }

    @Test
    void deleteProfileHappyPath() {
        ProfileService profileService = Mockito.mock(ProfileService.class);

        ProfileController controller = new ProfileController(profileService);

        Authentication auth = Mockito.mock(Authentication.class);
        Mockito.when(auth.isAuthenticated()).thenReturn(true);
        Mockito.when(auth.getName()).thenReturn("testuser");

        Mockito.doNothing().when(profileService).deleteProfile("testuser");

        ResponseEntity<?> resp = controller.deleteProfile(auth);
        assertEquals(204, resp.getStatusCode().value());
        Mockito.verify(profileService).deleteProfile("testuser");
    }
}
