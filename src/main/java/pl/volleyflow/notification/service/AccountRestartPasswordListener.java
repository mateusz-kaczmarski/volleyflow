package pl.volleyflow.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pl.volleyflow.auth.event.AccountRestartPasswordRequested;

@Component
@RequiredArgsConstructor
@Log4j2
public class AccountRestartPasswordListener {

    private final EmailService emailService;

    @KafkaListener(
            topics = "${app.kafka.topics.user-password-restart}",
            groupId = "volleyflow")
    public void sendRestartPasswordEmail(AccountRestartPasswordRequested passwordRequested) {
        log.info("Received password reset email request: eventId={}, userExternalId={}",
                passwordRequested.eventId(), passwordRequested.userAccountExternalId());
        emailService.sendPasswordResetEmail(passwordRequested);
        log.info("Password reset email submitted to SMTP: eventId={}, userExternalId={}",
                passwordRequested.eventId(), passwordRequested.userAccountExternalId());
    }
}
