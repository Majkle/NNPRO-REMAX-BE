package fei.upce.nnpro.remax.meetings.dto;

import fei.upce.nnpro.remax.meetings.entity.Meeting;
import fei.upce.nnpro.remax.meetings.entity.enums.MeetingStatus;
import fei.upce.nnpro.remax.meetings.entity.enums.MeetingType;
import fei.upce.nnpro.remax.profile.entity.Client;
import fei.upce.nnpro.remax.profile.entity.Realtor;
import fei.upce.nnpro.remax.realestates.entity.RealEstate;
import fei.upce.nnpro.remax.realestates.entity.House;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.ZonedDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class MeetingMapperTest {

    @InjectMocks
    private MeetingMapper meetingMapper;

    @Test
    void toDto_WithFullEntity_ShouldMapAllFields() {
        Meeting entity = new Meeting();
        entity.setId(1L);
        entity.setMeetingTime(ZonedDateTime.now());
        entity.setTitle("Property Viewing");
        entity.setDescription("First viewing of the property");
        entity.setMeetingType(MeetingType.OFFLINE);
        entity.setMeetingStatus(MeetingStatus.PENDING);

        RealEstate realEstate = new House();
        realEstate.setId(100L);
        entity.setRealEstate(realEstate);

        Realtor realtor = new Realtor();
        realtor.setId(10L);
        entity.setRealtor(realtor);

        Client client = new Client();
        client.setId(20L);
        entity.setClient(client);

        MeetingDto dto = meetingMapper.toDto(entity);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getMeetingTime()).isEqualTo(entity.getMeetingTime());
        assertThat(dto.getTitle()).isEqualTo("Property Viewing");
        assertThat(dto.getDescription()).isEqualTo("First viewing of the property");
        assertThat(dto.getMeetingType()).isEqualTo(MeetingType.OFFLINE);
        assertThat(dto.getMeetingStatus()).isEqualTo(MeetingStatus.PENDING);
        assertThat(dto.getRealEstateId()).isEqualTo(100L);
        assertThat(dto.getRealtorId()).isEqualTo(10L);
        assertThat(dto.getClientId()).isEqualTo(20L);
    }

    @Test
    void toDto_WithNullEntity_ShouldReturnNull() {
        MeetingDto dto = meetingMapper.toDto(null);
        assertThat(dto).isNull();
    }

    @Test
    void toDto_WithoutRealEstate_ShouldMapWithoutRealEstateId() {
        Meeting entity = new Meeting();
        entity.setId(1L);
        entity.setMeetingTime(ZonedDateTime.now());
        entity.setTitle("Consultation");
        entity.setDescription("Initial consultation");
        entity.setMeetingType(MeetingType.ONLINE);
        entity.setMeetingStatus(MeetingStatus.CONFIRMED);

        Realtor realtor = new Realtor();
        realtor.setId(10L);
        entity.setRealtor(realtor);

        Client client = new Client();
        client.setId(20L);
        entity.setClient(client);

        MeetingDto dto = meetingMapper.toDto(entity);

        assertThat(dto).isNotNull();
        assertThat(dto.getRealEstateId()).isNull();
        assertThat(dto.getRealtorId()).isEqualTo(10L);
        assertThat(dto.getClientId()).isEqualTo(20L);
    }

    @Test
    void toDto_WithoutRealtor_ShouldMapWithoutRealtorId() {
        Meeting entity = new Meeting();
        entity.setId(1L);
        entity.setMeetingTime(ZonedDateTime.now());
        entity.setTitle("Meeting");
        entity.setDescription("Description");
        entity.setMeetingType(MeetingType.ONLINE);
        entity.setMeetingStatus(MeetingStatus.CONFIRMED);

        RealEstate realEstate = new House();
        realEstate.setId(100L);
        entity.setRealEstate(realEstate);

        Client client = new Client();
        client.setId(20L);
        entity.setClient(client);

        MeetingDto dto = meetingMapper.toDto(entity);

        assertThat(dto).isNotNull();
        assertThat(dto.getRealEstateId()).isEqualTo(100L);
        assertThat(dto.getRealtorId()).isNull();
        assertThat(dto.getClientId()).isEqualTo(20L);
    }

    @Test
    void toDto_WithoutClient_ShouldMapWithoutClientId() {
        Meeting entity = new Meeting();
        entity.setId(1L);
        entity.setMeetingTime(ZonedDateTime.now());
        entity.setTitle("Meeting");
        entity.setDescription("Description");
        entity.setMeetingType(MeetingType.OFFLINE);
        entity.setMeetingStatus(MeetingStatus.CANCELED);

        RealEstate realEstate = new House();
        realEstate.setId(100L);
        entity.setRealEstate(realEstate);

        Realtor realtor = new Realtor();
        realtor.setId(10L);
        entity.setRealtor(realtor);

        MeetingDto dto = meetingMapper.toDto(entity);

        assertThat(dto).isNotNull();
        assertThat(dto.getRealEstateId()).isEqualTo(100L);
        assertThat(dto.getRealtorId()).isEqualTo(10L);
        assertThat(dto.getClientId()).isNull();
    }

    @Test
    void toEntity_WithFullDto_ShouldMapFields() {
        ZonedDateTime now = ZonedDateTime.now();
        MeetingDto dto = new MeetingDto();
        dto.setId(1L);
        dto.setMeetingTime(now);
        dto.setTitle("Property Viewing");
        dto.setDescription("First viewing");
        dto.setMeetingType(MeetingType.OFFLINE);
        dto.setMeetingStatus(MeetingStatus.PENDING);
        dto.setRealEstateId(100L);
        dto.setRealtorId(10L);
        dto.setClientId(20L);

        Meeting entity = meetingMapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getMeetingTime()).isEqualTo(now);
        assertThat(entity.getTitle()).isEqualTo("Property Viewing");
        assertThat(entity.getDescription()).isEqualTo("First viewing");
        assertThat(entity.getMeetingType()).isEqualTo(MeetingType.OFFLINE);
        assertThat(entity.getMeetingStatus()).isEqualTo(MeetingStatus.PENDING);
    }

    @Test
    void toEntity_WithNullDto_ShouldReturnNull() {
        Meeting entity = meetingMapper.toEntity(null);
        assertThat(entity).isNull();
    }

    @Test
    void toEntity_WithMinimalDto_ShouldMapRequiredFields() {
        ZonedDateTime now = ZonedDateTime.now();
        MeetingDto dto = new MeetingDto();
        dto.setMeetingTime(now);
        dto.setTitle("Quick Meet");
        dto.setMeetingType(MeetingType.ONLINE);
        dto.setMeetingStatus(MeetingStatus.CONFIRMED);

        Meeting entity = meetingMapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getMeetingTime()).isEqualTo(now);
        assertThat(entity.getTitle()).isEqualTo("Quick Meet");
        assertThat(entity.getMeetingType()).isEqualTo(MeetingType.ONLINE);
        assertThat(entity.getMeetingStatus()).isEqualTo(MeetingStatus.CONFIRMED);
        assertThat(entity.getDescription()).isNull();
    }
}
