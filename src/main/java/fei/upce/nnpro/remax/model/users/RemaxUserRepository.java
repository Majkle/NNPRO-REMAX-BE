package fei.upce.nnpro.remax.model.users;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RemaxUserRepository extends JpaRepository<RemaxUser, Long> {
    boolean existsByUsernameOrEmail(String username, String email);
    Optional<RemaxUser> findByUsername(String username);
    Optional<RemaxUser> findByEmail(String email);
}
