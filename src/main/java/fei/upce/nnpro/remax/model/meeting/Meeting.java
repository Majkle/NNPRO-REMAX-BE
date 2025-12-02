package fei.upce.nnpro.remax.model.meeting;

import fei.upce.nnpro.remax.model.meeting.enums.MeetingStatus;
import fei.upce.nnpro.remax.model.meeting.enums.MeetingType;
import fei.upce.nnpro.remax.model.realestates.entity.RealEstate;
import fei.upce.nnpro.remax.model.users.Client;
import fei.upce.nnpro.remax.model.users.Realtor;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@Entity
@Table(name = "meeting")
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "meeting_time", nullable = false)
    private ZonedDateTime meetingTime;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeetingType meetingType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MeetingStatus meetingStatus;

    @ManyToOne(optional = false)
    @JoinColumn(name = "real_estate_id", nullable = false)
    private RealEstate realEstate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "realtor_id", nullable = false)
    private Realtor realtor;

    @ManyToOne(optional = false)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
}