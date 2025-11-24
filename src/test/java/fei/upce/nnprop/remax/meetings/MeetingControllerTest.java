package fei.upce.nnprop.remax.meetings;

import fei.upce.nnprop.remax.meetings.controller.MeetingController;
import fei.upce.nnprop.remax.meetings.dto.MeetingDto;
import fei.upce.nnprop.remax.meetings.service.MeetingService;
import fei.upce.nnprop.remax.model.meeting.Meeting;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MeetingControllerTest {

    @Test
    void createMeetingHappyPath() {
        MeetingService svc = Mockito.mock(MeetingService.class);
        fei.upce.nnprop.remax.meetings.MeetingMapper mapper = Mockito.mock(fei.upce.nnprop.remax.meetings.MeetingMapper.class);
        MeetingController controller = new MeetingController(svc, mapper);

        MeetingDto dto = new MeetingDto();
        dto.setMeetingTime(ZonedDateTime.now());
        dto.setTitle("t");

        Meeting created = new Meeting();
        created.setId(5L);
        Mockito.when(svc.createMeeting(Mockito.any(MeetingDto.class))).thenReturn(created);
        Mockito.when(mapper.toDto(created)).thenReturn(dto);

        ResponseEntity<MeetingDto> resp = controller.createMeeting(dto);
        assertEquals(201, resp.getStatusCode().value());
        assertEquals(dto, resp.getBody());

        Mockito.verify(svc).createMeeting(Mockito.any(MeetingDto.class));
    }

    @Test
    void getMeetingHappyPath() {
        MeetingService svc = Mockito.mock(MeetingService.class);
        fei.upce.nnprop.remax.meetings.MeetingMapper mapper = Mockito.mock(fei.upce.nnprop.remax.meetings.MeetingMapper.class);
        MeetingController controller = new MeetingController(svc, mapper);

        Meeting m = new Meeting(); m.setId(3L);
        MeetingDto dto = new MeetingDto(); dto.setId(3L);
        Mockito.when(svc.getMeeting(3L)).thenReturn(m);
        Mockito.when(mapper.toDto(m)).thenReturn(dto);

        ResponseEntity<MeetingDto> resp = controller.getMeeting(3L);
        assertEquals(200, resp.getStatusCode().value());
        assertNotNull(resp.getBody());
        assertEquals(3L, resp.getBody().getId());
    }
}
