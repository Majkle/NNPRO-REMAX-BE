package fei.upce.nnpro.remax.security.auth;

import fei.upce.nnpro.remax.address.service.AddressService;
import fei.upce.nnpro.remax.mail.MailService;
import fei.upce.nnpro.remax.address.entity.Address;
import fei.upce.nnpro.remax.profile.entity.enums.AccountStatus;
import fei.upce.nnpro.remax.profile.dto.RemaxUserResponse;
import fei.upce.nnpro.remax.profile.entity.Client;
import fei.upce.nnpro.remax.profile.entity.PersonalInformation;
import fei.upce.nnpro.remax.profile.entity.RemaxUser;
import fei.upce.nnpro.remax.profile.repository.RemaxUserRepository;
import fei.upce.nnpro.remax.profile.service.PersonalInformationService;
import fei.upce.nnpro.remax.security.auth.request.RegisterRequest;
import fei.upce.nnpro.remax.security.auth.response.AuthResponse;
import fei.upce.nnpro.remax.security.config.SecurityProperties;
import fei.upce.nnpro.remax.security.jwt.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final RemaxUserRepository userRepository;
    private final AddressService addressService;
    private final PersonalInformationService personalInformationService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final SecurityProperties securityProperties;
    private final MailService mailService;

    public AuthService(RemaxUserRepository userRepository,
                       AddressService addressService,
                       PersonalInformationService personalInformationService,
                       AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil,
                       PasswordEncoder passwordEncoder, SecurityProperties securityProperties) {
        this(userRepository, addressService, personalInformationService, authenticationManager, jwtUtil, passwordEncoder, securityProperties, new fei.upce.nnpro.remax.mail.MailService());
    }

    public AuthService(RemaxUserRepository userRepository,
                       AddressService addressService,
                       PersonalInformationService personalInformationService,
                       AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil,
                       PasswordEncoder passwordEncoder, SecurityProperties securityProperties, MailService mailService) {
        this.userRepository = userRepository;
        this.addressService = addressService;
        this.personalInformationService = personalInformationService;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.securityProperties = securityProperties;
        this.mailService = mailService;
    }

    public AuthResponse login(String username, String password) {
        log.info("Attempting login for username={}", username);
        Optional<RemaxUser> maybeUser = userRepository.findByUsername(username);
        if (maybeUser.isEmpty()) {
            log.warn("Login failed for unknown username={}", username);
            throw new BadCredentialsException("Bad credentials");
        }
        RemaxUser user = maybeUser.get();

        // if user is blocked and blockedUntil in future, reject
        if (user.getAccountStatus() == AccountStatus.BLOCKED && user.getBlockedUntil() != null && user.getBlockedUntil().isAfter(ZonedDateTime.now())) {
            log.warn("Login attempt for blocked user={} until={}", username, user.getBlockedUntil());
            throw new BadCredentialsException("Account is blocked until: " + user.getBlockedUntil());
        }

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            // successful login -> reset failed attempts
            user.setFailedLoginAttempts(0);
            userRepository.save(user);

            String token = jwtUtil.generateToken(username);
            // compute expiry
            long expiresAt = System.currentTimeMillis() + securityProperties.getJwtExpirationMs();
            log.info("User {} logged in successfully", username);
            return new AuthResponse(token, expiresAt, RemaxUserResponse.getRole(user));
        } catch (AuthenticationException ex) {
            // failed login -> increment counter, possibly block
            int attempts = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(attempts);
            if (attempts >= securityProperties.getFailedLoginThreshold()) {
                // block for configured hours
                user.setAccountStatus(AccountStatus.BLOCKED);
                user.setBlockedUntil(ZonedDateTime.now().plusHours(securityProperties.getLockDurationHours()));
                log.warn("User {} blocked due to too many failed attempts until {}", username, user.getBlockedUntil());
            } else {
                log.warn("Failed login attempt {} for user {}", attempts, username);
            }
            userRepository.save(user);
            throw new BadCredentialsException("Bad credentials");
        }
    }

    public RemaxUser register(RegisterRequest request) {
        log.info("Registering user username={}", request.getUsername());
        Optional<RemaxUser> byUsername = userRepository.findByUsername(request.getUsername());
        if (byUsername.isPresent()) {
            log.warn("Attempt to register existing username={}", request.getUsername());
            throw new IllegalArgumentException("Username already exists");
        }
        Optional<RemaxUser> byEmail = userRepository.findByEmail(request.getEmail());
        if (byEmail.isPresent()) {
            log.warn("Attempt to register existing email={}", request.getEmail());
            throw new IllegalArgumentException("Email already exists");
        }

        Address savedAddress = addressService.createFrom(request);
        PersonalInformation pi = personalInformationService.createFrom(request, savedAddress);

        RemaxUser newUser = new Client();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setCreatedAt(OffsetDateTime.now());
        newUser.setAccountStatus(AccountStatus.NORMAL);
        newUser.setPersonalInformation(pi);

        RemaxUser savedUser = userRepository.save(newUser);
        log.info("Registered new user username={} id={}", savedUser.getUsername(), savedUser.getId());
        return savedUser;
    }

    public void requestPasswordReset(String email) {
        log.info("Password reset requested for email={}", email);
        Optional<RemaxUser> maybe = userRepository.findByEmail(email);
        if (maybe.isEmpty()) {
            log.warn("Password reset requested for unknown email={}", email);
            return;
        }
        RemaxUser user = maybe.get();
        String code = java.util.UUID.randomUUID().toString().replace("-", "").toUpperCase(); // FIXME more user-friendly and secure code - in DB encrypted/hashed
        user.setPasswordResetCode(code);
        user.setPasswordResetCodeDeadline(ZonedDateTime.now().plus(securityProperties.getPasswordResetTokenExpirationMs(), ChronoUnit.MILLIS));
        userRepository.save(user);

        mailService.sendPasswordResetCode(user.getEmail(), code);
        log.info("Password reset code generated for email={}", email);
    }

    public void resetPassword(String username, String code, String newPassword) {
        log.info("Attempting password reset with code={}", code);

        Optional<RemaxUser> maybe = userRepository.findByUsername(username);
        if (maybe.isEmpty()) {
            log.warn("Invalid username={}", username);
            throw new IllegalArgumentException("Invalid username");
        }
        RemaxUser user = maybe.get();
        if (user.getPasswordResetCode() == null || !user.getPasswordResetCode().equals(code)) {
            log.warn("Invalid password reset code={}", code);
            throw new IllegalArgumentException("Code invalid");
        }

        if (user.getPasswordResetCodeDeadline() == null || user.getPasswordResetCodeDeadline().isBefore(ZonedDateTime.now())) {
            log.warn("Password reset code expired for user={}", user.getEmail());
            throw new IllegalArgumentException("Code expired");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordResetCode(null);
        user.setPasswordResetCodeDeadline(null);
        userRepository.save(user);
        log.info("Password reset successful for user={}", user.getEmail());
    }
}
