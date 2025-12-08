package fei.upce.nnpro.remax.meetings.dto;

import fei.upce.nnpro.remax.meetings.entity.enums.MeetingStatus;
import fei.upce.nnpro.remax.meetings.entity.enums.MeetingType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@Schema(description = "Data Transfer Object representing a meeting appointment between a client and a realtor")
public class MeetingDto {

    @Schema(description = "Unique ID of the meeting", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotNull
    @Schema(description = "Scheduled date and time of the meeting in ISO-8601 format", example = "2025-10-15T14:30:00Z", type = "string", format = "date-time")
    private ZonedDateTime meetingTime;

    @NotBlank
    @Schema(description = "Short title or subject of the meeting", example = "Apartment Viewing - 2+kk Brno")
    private String title;

    @Schema(description = "Detailed notes or agenda for the meeting", example = "Client wants to inspect the wiring and discuss mortgage options.")
    private String description;

    @NotNull
    @Schema(description = "Type of meeting (e.g., ONLINE or OFFLINE)", example = "OFFLINE")
    private MeetingType meetingType;

    @NotNull
    @Schema(description = "Current status of the meeting", example = "PENDING")
    private MeetingStatus meetingStatus;

    // References by id
    @NotNull
    @Schema(description = "ID of the Real Estate property being discussed", example = "105")
    private Long realEstateId;

    @NotNull
    @Schema(description = "ID of the Realtor assigned to the meeting", example = "12")
    private Long realtorId;

    @NotNull
    @Schema(description = "ID of the Client requesting the meeting", example = "45")
    private Long clientId;

}

