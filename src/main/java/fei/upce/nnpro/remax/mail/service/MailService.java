package fei.upce.nnpro.remax.mail.service;

import fei.upce.nnpro.remax.mail.config.MailProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;

@Service
public class MailService {
    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final JavaMailSender mailSender;
    private final MailProperties mailProperties;

    public MailService(JavaMailSender mailSender, MailProperties mailProperties) {
        this.mailSender = mailSender;
        this.mailProperties = mailProperties;
    }

    public void sendPasswordResetCode(String to, String code) {
        log.info("Preparing password reset email to={}", to);
        String subject = "Žádost o obnovení hesla";
        try {
            String body = renderTemplate("templates/password-reset.html", Map.of("code", code, "email", to));

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            if (mailProperties.getFrom() != null) helper.setFrom(mailProperties.getFrom());
            if (mailProperties.getReplyTo() != null) helper.setReplyTo(mailProperties.getReplyTo());

            mailSender.send(message);
            log.info("Password reset email sent to={}", to);
        } catch (MessagingException ex) {
            log.error("Failed to prepare or send password reset email to={}", to, ex);
        } catch (Exception ex) {
            log.error("Unexpected error while sending password reset email to={}", to, ex);
        }
    }

    private String renderTemplate(String resourcePath, Map<String, String> params) {
        // resourcePath is relative to classpath, allow with or without leading '/'
        String cp = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(cp)) {
            if (in == null) {
                log.warn("Email template not found: {}", resourcePath);
                return fallbackPasswordResetText(params.get("code"));
            }
            try (Scanner s = new Scanner(in, StandardCharsets.UTF_8.name())) {
                s.useDelimiter("\\A");
                String template = s.hasNext() ? s.next() : "";
                for (Map.Entry<String, String> e : params.entrySet()) {
                    template = template.replace("{{" + e.getKey() + "}}", e.getValue() == null ? "" : e.getValue());
                }
                return template;
            }
        } catch (Exception ex) {
            log.error("Failed to render email template {}", resourcePath, ex);
            return fallbackPasswordResetText(params.get("code"));
        }
    }

    private String fallbackPasswordResetText(String code) {
        return "Your password reset code: " + code;
    }
}
