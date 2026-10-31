package pl.volleyflow.notification.service;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import pl.volleyflow.auth.event.AccountActivationEmailRequested;
import pl.volleyflow.auth.event.AccountRestartPasswordRequested;

import java.util.Properties;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    @InjectMocks
    private EmailService emailService;

    @Test
    void shouldSendActivationEmail() throws Exception {
        UUID userId = UUID.randomUUID();
        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
        ReflectionTestUtils.setField(emailService, "activationPageUrl", "https://example.com/activate");
        ReflectionTestUtils.setField(emailService, "mailFrom", "noreply@example.com");
        when(javaMailSender.createMimeMessage()).thenReturn(message);
        when(templateEngine.process(eq("mail/account-activation"), any(Context.class))).thenReturn("<p>Activate</p>");

        emailService.sendActivationEmail(new AccountActivationEmailRequested(
                UUID.randomUUID(), userId, "user@example.com", "Jan", "Kowalski", "token"));

        verify(javaMailSender).send(message);
        assertThat(message.getSubject()).isEqualTo("Aktywuj konto w VolleyFlow");
        assertThat(message.getAllRecipients()[0].toString()).isEqualTo("user@example.com");
        var context = org.mockito.ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("mail/account-activation"), context.capture());
        assertThat(context.getValue().getVariable("activationUrl").toString())
                .contains("userId=" + userId, "token=token");
    }

    @Test
    void shouldSendPasswordResetEmail() throws Exception {
        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
        ReflectionTestUtils.setField(emailService, "passwordResetPageUrl", "https://example.com/reset");
        ReflectionTestUtils.setField(emailService, "mailFrom", "noreply@example.com");
        when(javaMailSender.createMimeMessage()).thenReturn(message);
        when(templateEngine.process(eq("mail/account-password-reset"), any(Context.class)))
                .thenReturn("<p>Reset</p>");

        emailService.sendPasswordResetEmail(new AccountRestartPasswordRequested(
                UUID.randomUUID(), UUID.randomUUID(), "user@example.com", "reset-token"));

        verify(javaMailSender).send(message);
        assertThat(message.getSubject()).isEqualTo("Reset hasła w VolleyFlow");
        var context = org.mockito.ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("mail/account-password-reset"), context.capture());
        assertThat(context.getValue().getVariable("resetUrl").toString()).contains("token=reset-token");
    }
}
