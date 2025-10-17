package fei.upce.nnprop.remax.model.users;

import fei.upce.nnprop.remax.model.users.enums.UserAccountStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "remax_user")
public abstract class RemaxUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false)
    private UserAccountStatus accountStatus = UserAccountStatus.NORMAL;

    @Column(name = "email", nullable = false, unique = true)
    @Email
    private String email;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @OneToOne(optional = false, orphanRemoval = true)
    @JoinColumn(name = "personal_information_id", nullable = false)
    private PersonalInformation personalInformation;

}