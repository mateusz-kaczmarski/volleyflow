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

    @KafkaListener(
            topics = "${app.kafka.topics.user-account-created}",
            groupId = "volleyflow")
    public void sendRegistrationEmail(AccountActivationEmailRequested emailRequested) {
        //todo tmp solution, to do email service
        log.info("Successfully get event for eventId: {}, userId: {} ",
                emailRequested.eventId(), emailRequested.userExternalId());
    }


}
