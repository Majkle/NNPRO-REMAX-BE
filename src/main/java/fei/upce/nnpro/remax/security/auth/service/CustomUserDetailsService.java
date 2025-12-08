package fei.upce.nnpro.remax.security.auth.service;

import fei.upce.nnpro.remax.profile.entity.Admin;
import fei.upce.nnpro.remax.profile.entity.Realtor;
import fei.upce.nnpro.remax.profile.entity.RemaxUser;
import fei.upce.nnpro.remax.profile.repository.RemaxUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final RemaxUserRepository userRepository;

    public CustomUserDetailsService(RemaxUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        RemaxUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        // Map account status to enabled/disabled
        boolean enabled = user.getAccountStatus() != null;

        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        // assign role based on concrete class
        if (user instanceof Admin) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        if (user instanceof Realtor) {
            authorities.add(new SimpleGrantedAuthority("ROLE_REALTOR"));
        }
        // every user has ROLE_USER
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        // also expose ROLE_ANONYMOUS so code can authorize against it if needed
        authorities.add(new SimpleGrantedAuthority("ROLE_ANONYMOUS"));

        log.info("Loaded user {} with roles={}", username, authorities);

        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountLocked(false)
                .disabled(!enabled)
                .build();
    }
}
