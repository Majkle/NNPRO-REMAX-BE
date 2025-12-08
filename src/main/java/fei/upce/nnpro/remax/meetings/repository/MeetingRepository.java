package fei.upce.nnpro.remax.meetings.repository;

import fei.upce.nnpro.remax.meetings.entity.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
}