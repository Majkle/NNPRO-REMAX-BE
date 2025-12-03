package fei.upce.nnprop.remax.meetings.dto;

import fei.upce.nnprop.remax.model.meeting.enums.MeetingStatus;
import fei.upce.nnprop.remax.model.meeting.enums.MeetingType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@Schema(description = "Data transfer object for creating or updating a meeting")
public class MeetingDto {

    @Schema(description = "Meeting ID (null for creation)", example = "1")
    private Long id;

    @NotNull
    @Schema(description = "Date and time of the meeting (ISO-8601)", example = "2025-12-01T14:00:00Z")
    private ZonedDateTime meetingTime;

    @NotBlank
    @Schema(description = "Title of the meeting", example = "Apartment viewing")
    private String title;

    @Schema(description = "Detailed notes about the meeting", example = "Client wants to check the noise level.")
    private String description;

    @NotNull
    @Schema(description = "Type of meeting", example = "OFFLINE")
    private MeetingType meetingType;

    @NotNull
    @Schema(description = "Current status of the meeting", example = "SCHEDULED")
    private MeetingStatus meetingStatus;

    @NotNull
    @Schema(description = "ID of the real estate involved", example = "55")
    private Long realEstateId;

    @NotNull
    @Schema(description = "ID of the realtor handling the meeting", example = "2")
    private Long realtorId;

    @NotNull
    @Schema(description = "ID of the client requesting the meeting", example = "3")
    private Long clientId;

}

