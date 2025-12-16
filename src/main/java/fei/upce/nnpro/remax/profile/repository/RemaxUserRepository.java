package fei.upce.nnpro.remax.profile.repository;

import fei.upce.nnpro.remax.profile.entity.RemaxUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RemaxUserRepository extends JpaRepository<RemaxUser, Long> {
    boolean existsByUsernameOrEmail(String username, String email);
    Optional<RemaxUser> findByUsername(String username);
    Optional<RemaxUser> findByEmail(String email);

    @Query("SELECT u FROM RemaxUser u WHERE TYPE(u) = Realtor")
    List<RemaxUser> findAllRealtors();
}
