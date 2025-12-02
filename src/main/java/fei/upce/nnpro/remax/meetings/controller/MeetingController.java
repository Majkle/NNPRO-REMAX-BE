package fei.upce.nnpro.remax.meetings.controller;

import fei.upce.nnpro.remax.meetings.dto.MeetingDto;
import fei.upce.nnpro.remax.meetings.dto.MeetingMapper;
import fei.upce.nnpro.remax.meetings.entity.Meeting;
import fei.upce.nnpro.remax.meetings.service.MeetingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final MeetingService meetingService;
    private final MeetingMapper meetingMapper;

    @PostMapping
    public ResponseEntity<MeetingDto> createMeeting(@Valid @RequestBody MeetingDto dto) {
        Meeting created = meetingService.createMeeting(dto);
        return new ResponseEntity<>(meetingMapper.toDto(created), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MeetingDto> getMeeting(@PathVariable Long id) {
        Meeting entity = meetingService.getMeeting(id);
        return ResponseEntity.ok(meetingMapper.toDto(entity));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_REALTOR') or hasRole('ROLE_USER')")
    public ResponseEntity<MeetingDto> updateMeeting(@PathVariable Long id, @Valid @RequestBody MeetingDto dto) {
        Meeting updated = meetingService.updateMeeting(id, dto);
        return ResponseEntity.ok(meetingMapper.toDto(updated));
    }

    @GetMapping
    @PreAuthorize("hasRole('ROLE_REALTOR') or hasRole('ROLE_USER')")
    public ResponseEntity<Page<MeetingDto>> listMeetings(@ParameterObject Pageable pageable) {
        Page<Meeting> page = meetingService.searchMeetings(pageable);
        Page<MeetingDto> dtos = page.map(meetingMapper::toDto);
        return ResponseEntity.ok(dtos);
    }
}

