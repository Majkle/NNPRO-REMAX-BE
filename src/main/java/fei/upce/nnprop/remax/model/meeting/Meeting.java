package fei.upce.nnprop.remax.model.meeting;

import fei.upce.nnprop.remax.model.meeting.enums.MEETING_STATUS;
import fei.upce.nnprop.remax.model.meeting.enums.MEETING_TYPE;
import fei.upce.nnprop.remax.model.realestates.RealEstate;
import fei.upce.nnprop.remax.model.users.Client;
import fei.upce.nnprop.remax.model.users.Realtor;
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
    private MEETING_TYPE meetingType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MEETING_STATUS meetingStatus;

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