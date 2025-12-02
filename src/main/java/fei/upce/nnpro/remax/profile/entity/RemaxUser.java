package fei.upce.nnpro.remax.profile.entity;

import fei.upce.nnpro.remax.profile.entity.enums.AccountStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;

@Getter
@Setter
@Entity
@Table(name = "remax_user")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)
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
    private AccountStatus accountStatus = AccountStatus.NORMAL;

    @Column(name = "email", nullable = false, unique = true)
    @Email
    private String email;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @OneToOne(optional = false, orphanRemoval = true)
    @JoinColumn(name = "personal_information_id", nullable = false)
    private PersonalInformation personalInformation;

    @Column(name = "failed_login_Attempts", nullable = false)
    private int failedLoginAttempts = 0;

    @Column(name = "blocked_until")
    private ZonedDateTime blockedUntil;

    @Column(name = "password_reset_code")
    private String passwordResetCode;

    @Column(name = "password_reset_code_deadline")
    private ZonedDateTime passwordResetCodeDeadline;
}