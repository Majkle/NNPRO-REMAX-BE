package fei.upce.nnpro.remax.meetings.service;

import fei.upce.nnpro.remax.meetings.MeetingMapper;
import fei.upce.nnpro.remax.meetings.dto.MeetingDto;
import fei.upce.nnpro.remax.model.meeting.Meeting;
import fei.upce.nnpro.remax.model.meeting.MeetingRepository;
import fei.upce.nnpro.remax.model.users.Client;
import fei.upce.nnpro.remax.model.users.Realtor;
import fei.upce.nnpro.remax.model.users.RemaxUserRepository;
import fei.upce.nnpro.remax.realestates.repository.RealEstateRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final MeetingMapper meetingMapper;
    private final RealEstateRepository realEstateRepository;
    private final RemaxUserRepository remaxUserRepository;

    @Transactional
    public Meeting createMeeting(MeetingDto dto) {
        Meeting entity = meetingMapper.toEntity(dto);

        // Resolve relations by id to managed entities
        if (dto.getRealEstateId() != null) {
            entity.setRealEstate(realEstateRepository.findById(dto.getRealEstateId())
                    .orElseThrow(() -> new EntityNotFoundException("RealEstate not found")));
        }
        if (dto.getRealtorId() != null) {
            var user = remaxUserRepository.findById(dto.getRealtorId())
                    .orElseThrow(() -> new EntityNotFoundException("Realtor not found"));
            if (user instanceof Realtor) {
                entity.setRealtor((Realtor) user);
            } else {
                throw new EntityNotFoundException("Realtor not found");
            }
        }
        if (dto.getClientId() != null) {
            var user = remaxUserRepository.findById(dto.getClientId())
                    .orElseThrow(() -> new EntityNotFoundException("Client not found"));
            if (user instanceof Client) {
                entity.setClient((Client) user);
            } else {
                throw new EntityNotFoundException("Client not found");
            }
        }

        return meetingRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public Meeting getMeeting(Long id) {
        return meetingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Meeting with ID " + id + " not found"));
    }

    @Transactional
    public Meeting updateMeeting(Long id, MeetingDto dto) {
        Meeting existing = meetingRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Meeting with ID " + id + " not found"));

        if (dto.getMeetingTime() != null) existing.setMeetingTime(dto.getMeetingTime());
        if (dto.getTitle() != null) existing.setTitle(dto.getTitle());
        if (dto.getDescription() != null) existing.setDescription(dto.getDescription());
        if (dto.getMeetingType() != null) existing.setMeetingType(dto.getMeetingType());
        if (dto.getMeetingStatus() != null) existing.setMeetingStatus(dto.getMeetingStatus());

        if (dto.getRealEstateId() != null) {
            existing.setRealEstate(realEstateRepository.findById(dto.getRealEstateId())
                    .orElseThrow(() -> new EntityNotFoundException("RealEstate not found")));
        }
        if (dto.getRealtorId() != null) {
            var user = remaxUserRepository.findById(dto.getRealtorId())
                    .orElseThrow(() -> new EntityNotFoundException("Realtor not found"));
            if (user instanceof Realtor) {
                existing.setRealtor((Realtor) user);
            } else {
                throw new EntityNotFoundException("Realtor not found");
            }
        }
        if (dto.getClientId() != null) {
            var user = remaxUserRepository.findById(dto.getClientId())
                    .orElseThrow(() -> new EntityNotFoundException("Client not found"));
            if (user instanceof Client) {
                existing.setClient((Client) user);
            } else {
                throw new EntityNotFoundException("Client not found");
            }
        }

        return meetingRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public Page<Meeting> searchMeetings(Pageable pageable) {
        return meetingRepository.findAll(pageable);
    }
}
