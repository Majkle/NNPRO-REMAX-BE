package fei.upce.nnpro.remax.meetings.service;

import fei.upce.nnpro.remax.meetings.dto.MeetingDto;
import fei.upce.nnpro.remax.meetings.dto.MeetingMapper;
import fei.upce.nnpro.remax.meetings.dto.RealEstateSimplifiedDto;
import fei.upce.nnpro.remax.meetings.entity.Meeting;
import fei.upce.nnpro.remax.meetings.repository.MeetingRepository;
import fei.upce.nnpro.remax.profile.entity.Client;
import fei.upce.nnpro.remax.profile.entity.PersonalInformation;
import fei.upce.nnpro.remax.profile.entity.Realtor;
import fei.upce.nnpro.remax.profile.entity.RemaxUser;
import fei.upce.nnpro.remax.profile.repository.RemaxUserRepository;
import fei.upce.nnpro.remax.realestates.repository.RealEstateRepository;
import fei.upce.nnpro.remax.review.dto.RealtorSimplifiedDto;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final MeetingMapper meetingMapper;
    private final RealEstateRepository realEstateRepository;
    private final RemaxUserRepository remaxUserRepository;
    private static final Logger log = LoggerFactory.getLogger(MeetingService.class);

    @Transactional
    public Meeting createMeeting(MeetingDto dto) {
        log.info("Creating meeting for realEstateId={} realtorId={} clientId={}", dto.getRealEstateId(), dto.getRealtorId(), dto.getClientId());
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

        Meeting saved = meetingRepository.save(entity);
        log.info("Created meeting id={} time={}", saved.getId(), saved.getMeetingTime());
        return saved;
    }

    @Transactional(readOnly = true)
    public Meeting getMeeting(Long id) {
        log.info("Fetching meeting id={}", id);
        Meeting found = meetingRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Meeting not found id={}", id);
                    return new EntityNotFoundException("Meeting with ID " + id + " not found");
                });
        log.debug("Fetched meeting id={} title={}", found.getId(), found.getTitle());
        return found;
    }

    @Transactional
    public Meeting updateMeeting(Long id, MeetingDto dto) {
        log.info("Updating meeting id={}", id);
        Meeting existing = meetingRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Meeting not found for update id={}", id);
                    return new EntityNotFoundException("Meeting with ID " + id + " not found");
                });

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

        Meeting saved = meetingRepository.save(existing);
        log.info("Updated meeting id={} time={}", saved.getId(), saved.getMeetingTime());
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<Meeting> searchMeetings(Pageable pageable) {
        log.info("Searching meetings pageable={}", pageable);
        Page<Meeting> page = meetingRepository.findAll(pageable);
        log.debug("Found {} meetings in page", page.getNumberOfElements());
        return page;
    }

    @Transactional(readOnly = true)
    public List<RealEstateSimplifiedDto> listRealEstatesSimplified() {
        return realEstateRepository.findAll().stream()
                .map((r) -> {
                    RealEstateSimplifiedDto rs = new RealEstateSimplifiedDto();
                    rs.setId(r.getId());
                    rs.setTitle(r.getName());
                    return rs;
                }).toList();
    }

    @Transactional(readOnly = true)
    public List<MeetingDto> listMyMeetings(String username) {
        log.debug("Searching for user {}", username);
        var user = remaxUserRepository.findByUsername(username);
        if (user.isEmpty()) {
            log.debug("User {} not found", username);
            return new ArrayList<MeetingDto>();
        }

        RemaxUser remaxUser = user.get();
        log.info("Searching meetings of {}", remaxUser);
        return meetingRepository.findAllByClientIdOrRealtorId(remaxUser.getId(), remaxUser.getId())
                .stream().map(meetingMapper::toDto).toList();
    }
}
