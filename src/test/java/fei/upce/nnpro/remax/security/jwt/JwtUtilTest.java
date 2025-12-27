package fei.upce.nnpro.remax.security.jwt;

import fei.upce.nnpro.remax.security.config.SecurityProperties;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    @Test
    void generateAndValidateToken() {
        SecurityProperties p = new SecurityProperties();
        p.setJwtSecret("testsecretkeywhichislongenough123456testsecretkeywhichislongenough123456");
        p.setJwtExpirationMs(3600_000);
        JwtUtil util = new JwtUtil(p);
        String token = util.generateToken("alice");
        assertNotNull(token);
        assertTrue(util.validateToken(token, createUserDetails("alice")));
    }

    @Test
    void expiredTokenIsInvalid() throws InterruptedException {
        SecurityProperties p = new SecurityProperties();
        p.setJwtSecret("testsecretkeywhichislongenough123456testsecretkeywhichislongenough123456");
        p.setJwtExpirationMs(1);
        JwtUtil util = new JwtUtil(p);
        String token = util.generateToken("bob");
        assertNotNull(token);
        Thread.sleep(1500);
        assertFalse(util.validateToken(token, createUserDetails("bob")));
    }

    private UserDetails createUserDetails(String username) {
        return User.builder()
                .username(username)
                .password("1234")
                .authorities(new HashSet<>())
                .accountLocked(false)
                .disabled(false)
                .build();
    }
}
