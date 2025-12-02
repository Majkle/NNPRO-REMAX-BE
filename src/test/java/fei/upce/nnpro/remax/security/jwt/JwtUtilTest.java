package fei.upce.nnpro.remax.security.jwt;

import fei.upce.nnpro.remax.security.config.SecurityProperties;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    @Test
    void generateAndValidateToken() {
        SecurityProperties p = new SecurityProperties();
        p.setJwtSecret("test-secret-key-which-is-long-enough-123456");
        p.setJwtExpirationMs(3600_000);
        JwtUtil util = new JwtUtil(p);
        String token = util.generateToken("alice");
        assertNotNull(token);
        assertTrue(util.validateToken(token));
        assertEquals("alice", util.getUsernameFromToken(token));
    }

    @Test
    void expiredTokenIsInvalid() throws InterruptedException {
        SecurityProperties p = new SecurityProperties();
        p.setJwtSecret("test-secret-key-which-is-long-enough-123456");
        p.setJwtExpirationMs(1);
        JwtUtil util = new JwtUtil(p);
        String token = util.generateToken("bob");
        assertNotNull(token);
        Thread.sleep(1500);
        assertFalse(util.validateToken(token));
    }
}
