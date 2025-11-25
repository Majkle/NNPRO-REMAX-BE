package fei.upce.nnprop.remax.model.users;

import fei.upce.nnprop.remax.model.image.Image;
import fei.upce.nnprop.remax.model.realestates.entity.Address;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

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

    @Column(name = "birth_date", nullable = false)
    private ZonedDateTime birthDate;

    @OneToOne(optional = false, orphanRemoval = true)
    @JoinColumn(name = "address_id", nullable = false)
    private Address address;

    @OneToOne(orphanRemoval = true)
    @JoinColumn(name = "image_id")
    private Image image;

}