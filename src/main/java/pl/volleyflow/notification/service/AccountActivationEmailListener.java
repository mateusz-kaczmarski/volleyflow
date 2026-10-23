package pl.volleyflow.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import pl.volleyflow.auth.event.AccountActivationEmailRequested;

@Component
@RequiredArgsConstructor
@Log4j2
public class AccountActivationEmailListener {

    private final EmailService emailService;

    @KafkaListener(
            topics = "${app.kafka.topics.user-account-created}",
            groupId = "volleyflow")
    public void sendAccountActivationEmail(AccountActivationEmailRequested emailRequested) {
        log.info("Received activation email request: eventId={}, userExternalId={}",
                emailRequested.eventId(), emailRequested.userExternalId());
        emailService.sendActivationEmail(emailRequested);
        log.info("Activation email submitted to SMTP: eventId={}, userExternalId={}",
                emailRequested.eventId(), emailRequested.userExternalId());
    }
}
