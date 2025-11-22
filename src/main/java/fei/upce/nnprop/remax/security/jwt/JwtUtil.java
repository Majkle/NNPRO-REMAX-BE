package fei.upce.nnprop.remax.security.jwt;

import fei.upce.nnprop.remax.security.config.SecurityProperties;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class JwtUtil {

    private final byte[] secretBytes;
    private final long expirationMs;

    public JwtUtil(SecurityProperties properties) {
        this.secretBytes = properties.getJwtSecret().getBytes(StandardCharsets.UTF_8);
        this.expirationMs = properties.getJwtExpirationMs();
    }

    public String generateToken(String username) {
        long now = Instant.now().toEpochMilli();
        long exp = now + expirationMs;

        String header = base64UrlEncode("{\"alg\":\"HS256\",\"typ\":\"JWT\"}");
        String payload = base64UrlEncode(String.format("{\"sub\":\"%s\",\"iat\":%d,\"exp\":%d}", escape(username), now / 1000, exp / 1000));
        String unsignedToken = header + "." + payload;
        String signature = base64UrlEncode(hmacSha256(unsignedToken));
        return unsignedToken + "." + signature;
    }

    public String getUsernameFromToken(String token) {
        String payload = getPayload(token);
        if (payload == null) return null;
        Pattern p = Pattern.compile("\"sub\":\"(.*?)\"");
        Matcher m = p.matcher(payload);
        if (m.find()) return m.group(1);
        return null;
    }

    public boolean validateToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return false;
            String unsigned = parts[0] + "." + parts[1];
            String signature = parts[2];
            String computed = base64UrlEncode(hmacSha256(unsigned));
            if (!Objects.equals(computed, signature)) return false;

            String payload = new String(Base64.getUrlDecoder().decode(padBase64(parts[1])), StandardCharsets.UTF_8);
            Pattern p = Pattern.compile("\"exp\":(\\d+)");
            Matcher m = p.matcher(payload);
            if (!m.find()) return false;
            long exp = Long.parseLong(m.group(1));
            long nowSec = Instant.now().getEpochSecond();
            return nowSec < exp;
        } catch (Exception e) {
            return false;
        }
    }

    private byte[] hmacSha256(String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secretBytes, "HmacSHA256");
            mac.init(keySpec);
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String base64UrlEncode(String str) {
        return base64UrlEncode(str.getBytes(StandardCharsets.UTF_8));
    }

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String getPayload(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) return null;
        try {
            return new String(Base64.getUrlDecoder().decode(padBase64(parts[1])), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String padBase64(String s) {
        int mod = s.length() % 4;
        if (mod == 2) return s + "==";
        if (mod == 3) return s + "=";
        return s;
    }
}
