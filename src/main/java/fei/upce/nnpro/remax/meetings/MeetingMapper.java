package fei.upce.nnpro.remax.meetings;

import fei.upce.nnpro.remax.meetings.dto.MeetingDto;
import fei.upce.nnpro.remax.model.meeting.Meeting;
import org.springframework.stereotype.Component;

@Component
public class MeetingMapper {

    public MeetingDto toDto(Meeting entity) {
        if (entity == null) return null;
        MeetingDto dto = new MeetingDto();
        dto.setId(entity.getId());
        dto.setMeetingTime(entity.getMeetingTime());
        dto.setTitle(entity.getTitle());
        dto.setDescription(entity.getDescription());
        dto.setMeetingType(entity.getMeetingType());
        dto.setMeetingStatus(entity.getMeetingStatus());

        if (entity.getRealEstate() != null) dto.setRealEstateId(entity.getRealEstate().getId());
        if (entity.getRealtor() != null) dto.setRealtorId(entity.getRealtor().getId());
        if (entity.getClient() != null) dto.setClientId(entity.getClient().getId());

        return dto;
    }

    public Meeting toEntity(MeetingDto dto) {
        if (dto == null) return null;
        Meeting m = new Meeting();
        m.setMeetingTime(dto.getMeetingTime());
        m.setTitle(dto.getTitle());
        m.setDescription(dto.getDescription());
        m.setMeetingType(dto.getMeetingType());
        m.setMeetingStatus(dto.getMeetingStatus());

        // NOTE: relations (realEstate, realtor, client) are resolved in MeetingService
        return m;
    }
}
