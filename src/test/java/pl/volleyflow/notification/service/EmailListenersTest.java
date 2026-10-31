package pl.volleyflow.notification.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.volleyflow.auth.event.AccountActivationEmailRequested;
import pl.volleyflow.auth.event.AccountRestartPasswordRequested;

import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EmailListenersTest {

    @Mock
    private EmailService emailService;

    @Test
    void shouldForwardActivationMessageToEmailService() {
        AccountActivationEmailRequested event = new AccountActivationEmailRequested(
                UUID.randomUUID(), UUID.randomUUID(), "user@example.com", "Jan", "Kowalski", "token");

        new AccountActivationEmailListener(emailService).sendAccountActivationEmail(event);

        verify(emailService).sendActivationEmail(event);
    }

    @Test
    void shouldForwardPasswordResetMessageToEmailService() {
        AccountRestartPasswordRequested event = new AccountRestartPasswordRequested(
                UUID.randomUUID(), UUID.randomUUID(), "user@example.com", "token");

        new PasswordResetEmailKafkaListener(emailService).sendRestartPasswordEmail(event);

        verify(emailService).sendPasswordResetEmail(event);
    }
}
