package fei.upce.nnprop.remax.meetings.controller;

import fei.upce.nnprop.remax.meetings.MeetingMapper;
import fei.upce.nnprop.remax.meetings.dto.MeetingDto;
import fei.upce.nnprop.remax.meetings.service.MeetingService;
import fei.upce.nnprop.remax.model.meeting.Meeting;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

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
    public ResponseEntity<MeetingDto> updateMeeting(@PathVariable Long id, @Valid @RequestBody MeetingDto dto) {
        Meeting updated = meetingService.updateMeeting(id, dto);
        return ResponseEntity.ok(meetingMapper.toDto(updated));
    }

    @GetMapping
    public ResponseEntity<Page<MeetingDto>> listMeetings(@ParameterObject Pageable pageable) {
        Page<Meeting> page = meetingService.searchMeetings(pageable);
        Page<MeetingDto> dtos = page.map(meetingMapper::toDto);
        return ResponseEntity.ok(dtos);
    }
}

