package fei.upce.nnpro.remax.meetings.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fei.upce.nnpro.remax.meetings.dto.MeetingDto;
import fei.upce.nnpro.remax.meetings.dto.MeetingMapper;
import fei.upce.nnpro.remax.meetings.entity.Meeting;
import fei.upce.nnpro.remax.meetings.service.MeetingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.ZonedDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MeetingControllerTest {

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private MeetingService meetingService;

    @Mock
    private MeetingMapper meetingMapper;

    @InjectMocks
    private MeetingController meetingController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(meetingController)
                .build();
    }

    @Test
    @Disabled
    void createMeeting_returnsCreated() throws Exception {
        MeetingDto dto = new MeetingDto();
        dto.setTitle("t");
        dto.setMeetingTime(ZonedDateTime.now());

        Meeting created = new Meeting();
        Mockito.when(meetingService.createMeeting(any(MeetingDto.class))).thenReturn(created);
        Mockito.when(meetingMapper.toDto(created)).thenReturn(dto);

        mockMvc.perform(post("/api/meetings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("t"));
    }

    @Test
    void getMeeting_returnsOk() throws Exception {
        Meeting m = new Meeting();
        MeetingDto dto = new MeetingDto();
        dto.setTitle("got");
        Mockito.when(meetingService.getMeeting(1L)).thenReturn(m);
        Mockito.when(meetingMapper.toDto(m)).thenReturn(dto);

        mockMvc.perform(get("/api/meetings/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("got"));
    }

    @Test
    @Disabled
    void listMeetings_returnsPage() throws Exception {
        Meeting m = new Meeting();
        MeetingDto dto = new MeetingDto();
        dto.setTitle("p");
        Mockito.when(meetingService.searchMeetings(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(m)));
        Mockito.when(meetingMapper.toDto(m)).thenReturn(dto);

        mockMvc.perform(get("/api/meetings")
                        .principal(new TestingAuthenticationToken("user", "p", "ROLE_USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("p"));
    }
}
