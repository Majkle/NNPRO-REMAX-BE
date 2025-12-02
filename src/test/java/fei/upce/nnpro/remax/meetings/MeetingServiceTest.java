package fei.upce.nnpro.remax.meetings;

import fei.upce.nnpro.remax.model.realestates.entity.Apartment;
import fei.upce.nnpro.remax.meetings.service.MeetingService;
import fei.upce.nnpro.remax.meetings.dto.MeetingDto;
import fei.upce.nnpro.remax.model.meeting.Meeting;
import fei.upce.nnpro.remax.model.meeting.MeetingRepository;
import fei.upce.nnpro.remax.realestates.repository.RealEstateRepository;
import fei.upce.nnpro.remax.model.users.RemaxUserRepository;
import fei.upce.nnpro.remax.model.users.Realtor;
import fei.upce.nnpro.remax.model.users.Client;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import jakarta.persistence.EntityNotFoundException;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
}
