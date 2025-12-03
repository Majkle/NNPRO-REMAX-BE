package fei.upce.nnpro.remax.mail;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class MailServiceTest {

    private JavaMailSender mailSender;
    private MailProperties props;
    private MailService mailService;

    @BeforeEach
    void setUp() {
        mailSender = Mockito.mock(JavaMailSender.class);
        props = new MailProperties();
        props.setFrom("no-reply@test.example");
        props.setReplyTo("reply@test.example");
        MimeMessage msg = Mockito.mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(msg);
        mailService = new MailService(mailSender, props);
    }

    @Test
    void sendPasswordResetCode_shouldSendMessage() {
        String to = "user@example.com";
        String code = "ABC123";

        mailService.sendPasswordResetCode(to, code);

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender, times(1)).send(captor.capture());
        MimeMessage sent = captor.getValue();
        assertThat(sent).isNotNull();
    }
}

