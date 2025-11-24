package fei.upce.nnprop.remax.meetings.dto;

import fei.upce.nnprop.remax.model.meeting.enums.MeetingStatus;
import fei.upce.nnprop.remax.model.meeting.enums.MeetingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
public class MeetingDto {

    private Long id;

    @NotNull
    private ZonedDateTime meetingTime;

    @NotBlank
    private String title;

    private String description;

    @NotNull
    private MeetingType meetingType;

    @NotNull
    private MeetingStatus meetingStatus;

    // References by id
    @NotNull
    private Long realEstateId;

    @NotNull
    private Long realtorId;

    @NotNull
    private Long clientId;

}

