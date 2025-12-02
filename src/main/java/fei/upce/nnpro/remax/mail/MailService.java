package fei.upce.nnpro.remax.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MailService {
    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    public void sendPasswordResetCode(String to, String code) {
        // Placeholder implementation: log the outgoing email. Replace with real mail sender.
        log.info("Sending password reset code to {}: {}", to, code);
    }
}

