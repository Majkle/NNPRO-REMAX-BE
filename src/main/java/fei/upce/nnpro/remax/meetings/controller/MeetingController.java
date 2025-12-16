package fei.upce.nnpro.remax.meetings.controller;

import fei.upce.nnpro.remax.meetings.dto.MeetingDto;
import fei.upce.nnpro.remax.meetings.dto.MeetingMapper;
import fei.upce.nnpro.remax.meetings.dto.RealEstateSimplifiedDto;
import fei.upce.nnpro.remax.meetings.entity.Meeting;
import fei.upce.nnpro.remax.meetings.service.MeetingService;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
@Tag(name = "Meetings", description = "Management of appointments between clients and agents")
@SecurityRequirement(name = "bearerAuth")
@Slf4j
public class MeetingController {

    private final MeetingService meetingService;
    private final MeetingMapper meetingMapper;

    @Operation(summary = "Create a new meeting",
            description = "Creates a meeting request linking a Client, a Realtor, and a Real Estate property.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Meeting successfully created"),
            @ApiResponse(responseCode = "400", description = "Invalid input data (validation error)", content = @Content),
            @ApiResponse(responseCode = "404", description = "Referenced Realtor, Client, or Real Estate not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @PostMapping
    public ResponseEntity<MeetingDto> createMeeting(@Valid @RequestBody MeetingDto dto) {
        log.info("Creating a new meeting");
        Meeting created = meetingService.createMeeting(dto);
        log.info("Meeting created");
        return new ResponseEntity<>(meetingMapper.toDto(created), HttpStatus.CREATED);
    }

    @Operation(summary = "Get meeting by ID", description = "Retrieves details of a specific meeting.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Meeting found"),
            @ApiResponse(responseCode = "404", description = "Meeting not found", content = @Content),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<MeetingDto> getMeeting(
            @Parameter(description = "ID of the meeting to retrieve")
            @PathVariable Long id) {
        Meeting entity = meetingService.getMeeting(id);
        return ResponseEntity.ok(meetingMapper.toDto(entity));
    }

    @Operation(summary = "Update an existing meeting",
            description = "Updates meeting details. Requires ROLE_REALTOR or ROLE_USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Meeting successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input data", content = @Content),
            @ApiResponse(responseCode = "404", description = "Meeting or referenced entities not found", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission", content = @Content)
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_REALTOR') or hasRole('ROLE_USER')")
    public ResponseEntity<MeetingDto> updateMeeting(
            @Parameter(description = "ID of the meeting to update")
            @PathVariable Long id, @Valid @RequestBody MeetingDto dto) {
        Meeting updated = meetingService.updateMeeting(id, dto);
        return ResponseEntity.ok(meetingMapper.toDto(updated));
    }

    @Operation(summary = "List all meetings",
            description = "Retrieve a paginated list of meetings. Requires ROLE_REALTOR or ROLE_USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of meetings retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission", content = @Content)
    })
    @GetMapping
    @PreAuthorize("hasRole('ROLE_REALTOR') or hasRole('ROLE_USER')")
    public ResponseEntity<Page<MeetingDto>> listMeetings(@ParameterObject Pageable pageable) {
        Page<Meeting> page = meetingService.searchMeetings(pageable);
        Page<MeetingDto> dtos = page.map(meetingMapper::toDto);
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Get all RealEstates simplified", description = "Retrieves a list of all RealEstates simplified.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of simplified RealEstates retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/estates")
    @PreAuthorize("hasRole('ROLE_REALTOR') or hasRole('ROLE_USER')")
    public ResponseEntity<List<RealEstateSimplifiedDto>> listRealEstates(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(meetingService.listRealEstatesSimplified());
    }

    @Operation(summary = "List all meetings of current user",
            description = "Retrieve a list of meetings of current user. Requires ROLE_REALTOR or ROLE_USER.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of meetings retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token", content = @Content),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have permission", content = @Content)
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/my")
    @PreAuthorize("hasRole('ROLE_REALTOR') or hasRole('ROLE_USER')")
    public ResponseEntity<List<MeetingDto>> listMyMeetings(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(meetingService.listMyMeetings(authentication.getName()));
    }
}

