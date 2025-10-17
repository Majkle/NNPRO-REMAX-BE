package fei.upce.nnprop.remax.model.users;

import fei.upce.nnprop.remax.model.Image;
import fei.upce.nnprop.remax.model.real_estates.Address;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "personal_information")
public class PersonalInformation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "degree")
    private String degree;
    
    @Column(name = "first_name", nullable = false)
    private String firstName;
    
    @Column(name = "last_name", nullable = false)
    private String lastName;
    
    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "brith_date", nullable = false)
    private ZonedDateTime brithDate;

    @OneToOne(optional = false, orphanRemoval = true)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "civic_anemities_id")
    private Image image;

}