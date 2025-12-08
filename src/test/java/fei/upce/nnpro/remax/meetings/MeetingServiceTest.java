package fei.upce.nnpro.remax.meetings;

import fei.upce.nnpro.remax.meetings.dto.MeetingDto;
import fei.upce.nnpro.remax.meetings.dto.MeetingMapper;
import fei.upce.nnpro.remax.meetings.entity.Meeting;
import fei.upce.nnpro.remax.meetings.repository.MeetingRepository;
import fei.upce.nnpro.remax.meetings.service.MeetingService;
import fei.upce.nnpro.remax.profile.entity.Client;
import fei.upce.nnpro.remax.profile.entity.Realtor;
import fei.upce.nnpro.remax.profile.repository.RemaxUserRepository;
import fei.upce.nnpro.remax.realestates.entity.Apartment;
import fei.upce.nnpro.remax.realestates.repository.RealEstateRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.data.domain.Page;
import fei.upce.nnpro.remax.meetings.entity.enums.MeetingType;
import fei.upce.nnpro.remax.meetings.entity.enums.MeetingStatus;

@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private MeetingMapper meetingMapper;

    @Mock
    private RealEstateRepository realEstateRepository;

    @Mock
    private RemaxUserRepository remaxUserRepository;

    @InjectMocks
    private MeetingService meetingService;

    @Test
    @DisplayName("Create: should save and return meeting when related entities exist")
    void createMeeting_Success() {
        MeetingDto dto = new MeetingDto();
        dto.setMeetingTime(ZonedDateTime.now());
        dto.setTitle("T");
        dto.setDescription("D");
        dto.setMeetingType(null);
        dto.setMeetingStatus(null);
        dto.setRealEstateId(1L);
        dto.setRealtorId(2L);
        dto.setClientId(3L);

        Meeting mapped = new Meeting();
        when(meetingMapper.toEntity(dto)).thenReturn(mapped);

        when(realEstateRepository.findById(1L)).thenReturn(Optional.of(new Apartment()));
        Realtor realtor = new Realtor(); realtor.setId(2L);
        when(remaxUserRepository.findById(2L)).thenReturn(Optional.of(realtor));
        Client client = new Client(); client.setId(3L);
        when(remaxUserRepository.findById(3L)).thenReturn(Optional.of(client));

        when(meetingRepository.save(any(Meeting.class))).thenAnswer(i -> i.getArgument(0));

        Meeting result = meetingService.createMeeting(dto);

        assertThat(result).isNotNull();
        verify(meetingRepository).save(mapped);
    }

    @Test
    @DisplayName("Create: should throw when realtor not found or wrong type")
    void createMeeting_RealtorMissing() {
        MeetingDto dto = new MeetingDto();
        dto.setRealEstateId(1L);
        dto.setRealtorId(99L);
        dto.setClientId(3L);

        when(meetingMapper.toEntity(dto)).thenReturn(new Meeting());
        when(realEstateRepository.findById(any())).thenReturn(Optional.of(new Apartment()));
        when(remaxUserRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> meetingService.createMeeting(dto)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Get: should return meeting when found")
    void getMeeting_Found() {
        Meeting m = new Meeting(); m.setId(1L);
        when(meetingRepository.findById(1L)).thenReturn(Optional.of(m));
        Meeting res = meetingService.getMeeting(1L);
        assertThat(res).isEqualTo(m);
    }

    @Test
    @DisplayName("Get: should throw when not found")
    void getMeeting_NotFound() {
        when(meetingRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> meetingService.getMeeting(1L)).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Update: should update all fields when provided")
    @Disabled
    void updateMeeting_AllFieldsProvided() {
        Long meetingId = 1L;
        MeetingDto dto = new MeetingDto();
        ZonedDateTime newTime = ZonedDateTime.now().plusHours(1);
        dto.setMeetingTime(newTime);
        dto.setTitle("Updated Title");
        dto.setDescription("Updated Description");
        dto.setMeetingType(MeetingType.ONLINE);
        dto.setMeetingStatus(MeetingStatus.CONFIRMED);
        dto.setRealEstateId(10L);
        dto.setRealtorId(20L);
        dto.setClientId(30L);

        Meeting existing = new Meeting();
        existing.setId(meetingId);
        existing.setTitle("Old Title");
        existing.setDescription("Old Description");

        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(existing));
        when(realEstateRepository.findById(10L)).thenReturn(Optional.of(new Apartment()));
        Realtor realtor = new Realtor(); realtor.setId(20L);
        when(remaxUserRepository.findById(20L)).thenReturn(Optional.of(realtor));
        Client client = new Client(); client.setId(30L);
        when(remaxUserRepository.findById(30L)).thenReturn(Optional.of(client));
        when(meetingRepository.save(any(Meeting.class))).thenAnswer(i -> i.getArgument(0));

        Meeting result = meetingService.updateMeeting(meetingId, dto);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Updated Title");
        assertThat(result.getDescription()).isEqualTo("Updated Description");
        assertThat(result.getMeetingType()).isEqualTo("ONLINE");
        assertThat(result.getMeetingStatus()).isEqualTo("CONFIRMED");
        verify(meetingRepository).save(existing);
    }

    @Test
    @DisplayName("Update: should update only provided fields and skip null ones")
    void updateMeeting_PartialFieldsProvided() {
        Long meetingId = 2L;
        MeetingDto dto = new MeetingDto();
        dto.setTitle("New Title");
        dto.setMeetingTime(null);
        dto.setDescription(null);
        dto.setMeetingType(null);
        dto.setMeetingStatus(null);
        dto.setRealEstateId(null);
        dto.setRealtorId(null);
        dto.setClientId(null);

        Meeting existing = new Meeting();
        existing.setId(meetingId);
        existing.setTitle("Old Title");
        existing.setMeetingTime(ZonedDateTime.now());

        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(existing));
        when(meetingRepository.save(any(Meeting.class))).thenAnswer(i -> i.getArgument(0));

        Meeting result = meetingService.updateMeeting(meetingId, dto);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("New Title");
        verify(meetingRepository).save(existing);
    }

    @Test
    @DisplayName("Update: should throw when meeting not found")
    void updateMeeting_MeetingNotFound() {
        MeetingDto dto = new MeetingDto();
        dto.setTitle("Title");

        when(meetingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> meetingService.updateMeeting(99L, dto))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Update: should throw when realEstateId not found")
    void updateMeeting_RealEstateNotFound() {
        Long meetingId = 1L;
        MeetingDto dto = new MeetingDto();
        dto.setRealEstateId(99L);

        Meeting existing = new Meeting();
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(existing));
        when(realEstateRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> meetingService.updateMeeting(meetingId, dto))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Update: should throw when realtor not found")
    void updateMeeting_RealtorNotFound() {
        Long meetingId = 1L;
        MeetingDto dto = new MeetingDto();
        dto.setRealtorId(99L);

        Meeting existing = new Meeting();
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(existing));
        when(remaxUserRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> meetingService.updateMeeting(meetingId, dto))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Update: should throw when realtor is wrong type")
    void updateMeeting_RealtorWrongType() {
        Long meetingId = 1L;
        MeetingDto dto = new MeetingDto();
        dto.setRealtorId(99L);

        Meeting existing = new Meeting();
        Client wrongType = new Client(); // Not a Realtor
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(existing));
        when(remaxUserRepository.findById(99L)).thenReturn(Optional.of(wrongType));

        assertThatThrownBy(() -> meetingService.updateMeeting(meetingId, dto))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Update: should throw when client not found")
    void updateMeeting_ClientNotFound() {
        Long meetingId = 1L;
        MeetingDto dto = new MeetingDto();
        dto.setClientId(99L);

        Meeting existing = new Meeting();
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(existing));
        when(remaxUserRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> meetingService.updateMeeting(meetingId, dto))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Update: should throw when client is wrong type")
    void updateMeeting_ClientWrongType() {
        Long meetingId = 1L;
        MeetingDto dto = new MeetingDto();
        dto.setClientId(99L);

        Meeting existing = new Meeting();
        Realtor wrongType = new Realtor(); // Not a Client
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(existing));
        when(remaxUserRepository.findById(99L)).thenReturn(Optional.of(wrongType));

        assertThatThrownBy(() -> meetingService.updateMeeting(meetingId, dto))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Create: should throw when realEstate not found")
    void createMeeting_RealEstateMissing() {
        MeetingDto dto = new MeetingDto();
        dto.setRealEstateId(99L);
        dto.setRealtorId(2L);
        dto.setClientId(3L);

        when(meetingMapper.toEntity(dto)).thenReturn(new Meeting());
        when(realEstateRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> meetingService.createMeeting(dto))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Create: should throw when realtor is wrong type")
    void createMeeting_RealtorWrongType() {
        MeetingDto dto = new MeetingDto();
        dto.setRealEstateId(1L);
        dto.setRealtorId(2L);
        dto.setClientId(3L);

        Meeting mapped = new Meeting();
        Client wrongType = new Client(); // Not a Realtor
        when(meetingMapper.toEntity(dto)).thenReturn(mapped);
        when(realEstateRepository.findById(1L)).thenReturn(Optional.of(new Apartment()));
        when(remaxUserRepository.findById(2L)).thenReturn(Optional.of(wrongType));

        assertThatThrownBy(() -> meetingService.createMeeting(dto))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Create: should throw when client not found or wrong type")
    void createMeeting_ClientMissing() {
        MeetingDto dto = new MeetingDto();
        dto.setRealEstateId(1L);
        dto.setRealtorId(2L);
        dto.setClientId(99L);

        when(meetingMapper.toEntity(dto)).thenReturn(new Meeting());
        when(realEstateRepository.findById(1L)).thenReturn(Optional.of(new Apartment()));
        Realtor realtor = new Realtor(); realtor.setId(2L);
        when(remaxUserRepository.findById(2L)).thenReturn(Optional.of(realtor));
        when(remaxUserRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> meetingService.createMeeting(dto))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Create: should throw when client is wrong type")
    void createMeeting_ClientWrongType() {
        MeetingDto dto = new MeetingDto();
        dto.setRealEstateId(1L);
        dto.setRealtorId(2L);
        dto.setClientId(3L);

        Meeting mapped = new Meeting();
        Realtor wrongType = new Realtor(); // Not a Client
        when(meetingMapper.toEntity(dto)).thenReturn(mapped);
        when(realEstateRepository.findById(1L)).thenReturn(Optional.of(new Apartment()));
        Realtor realtor = new Realtor(); realtor.setId(2L);
        when(remaxUserRepository.findById(2L)).thenReturn(Optional.of(realtor));
        when(remaxUserRepository.findById(3L)).thenReturn(Optional.of(wrongType));

        assertThatThrownBy(() -> meetingService.createMeeting(dto))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("Create: should handle null related entities gracefully")
    void createMeeting_NullRelatedEntities() {
        MeetingDto dto = new MeetingDto();
        dto.setMeetingTime(ZonedDateTime.now());
        dto.setTitle("Meeting");
        dto.setRealEstateId(null);
        dto.setRealtorId(null);
        dto.setClientId(null);

        Meeting mapped = new Meeting();
        when(meetingMapper.toEntity(dto)).thenReturn(mapped);
        when(meetingRepository.save(any(Meeting.class))).thenAnswer(i -> i.getArgument(0));

        Meeting result = meetingService.createMeeting(dto);

        assertThat(result).isNotNull();
        verify(meetingRepository).save(mapped);
    }

    @Test
    @DisplayName("Search: should return paginated meetings")
    void searchMeetings_Success() {
        org.springframework.data.domain.PageRequest pageable = org.springframework.data.domain.PageRequest.of(0, 10);

        Meeting m1 = new Meeting(); m1.setId(1L);
        Meeting m2 = new Meeting(); m2.setId(2L);
        java.util.List<Meeting> content = java.util.List.of(m1, m2);

        org.springframework.data.domain.PageImpl<Meeting> page = new org.springframework.data.domain.PageImpl<>(content, pageable, 2);
        when(meetingRepository.findAll(pageable)).thenReturn(page);

        Page<Meeting> result = meetingService.searchMeetings(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getNumberOfElements()).isEqualTo(2);
        assertThat(result.getContent()).contains(m1, m2);
    }

    @Test
    @DisplayName("Search: should return empty page when no meetings")
    void searchMeetings_EmptyResult() {
        org.springframework.data.domain.PageRequest pageable = org.springframework.data.domain.PageRequest.of(0, 10);

        org.springframework.data.domain.PageImpl<Meeting> page = new org.springframework.data.domain.PageImpl<>(java.util.List.of(), pageable, 0);
        when(meetingRepository.findAll(pageable)).thenReturn(page);

        Page<Meeting> result = meetingService.searchMeetings(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getNumberOfElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("Get: should handle different entity states")
    void getMeeting_DifferentEntityStates() {
        Meeting m = new Meeting();
        m.setId(5L);
        m.setTitle("Test Meeting");
        m.setDescription("Test Description");

        when(meetingRepository.findById(5L)).thenReturn(Optional.of(m));

        Meeting result = meetingService.getMeeting(5L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(5L);
        assertThat(result.getTitle()).isEqualTo("Test Meeting");
    }

    @Test
    @DisplayName("Update: should update with multiple related entities at once")
    void updateMeeting_MultipleEntitiesUpdate() {
        Long meetingId = 1L;
        MeetingDto dto = new MeetingDto();
        dto.setTitle("New Title");
        dto.setRealEstateId(10L);
        dto.setRealtorId(20L);
        dto.setClientId(30L);

        Meeting existing = new Meeting();
        when(meetingRepository.findById(meetingId)).thenReturn(Optional.of(existing));
        when(realEstateRepository.findById(10L)).thenReturn(Optional.of(new Apartment()));
        Realtor realtor = new Realtor(); realtor.setId(20L);
        when(remaxUserRepository.findById(20L)).thenReturn(Optional.of(realtor));
        Client client = new Client(); client.setId(30L);
        when(remaxUserRepository.findById(30L)).thenReturn(Optional.of(client));
        when(meetingRepository.save(any(Meeting.class))).thenAnswer(i -> i.getArgument(0));

        Meeting result = meetingService.updateMeeting(meetingId, dto);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("New Title");
        verify(meetingRepository).save(existing);
    }
}
