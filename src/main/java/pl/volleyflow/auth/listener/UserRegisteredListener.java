package pl.volleyflow.auth.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.volleyflow.auth.event.UserRegisteredEvent;
import pl.volleyflow.auth.service.AccountActivationService;

@Component
@RequiredArgsConstructor
@Log4j2
public class UserRegisteredListener {

    private final AccountActivationService accountActivationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void getCreateAccountEvent(UserRegisteredEvent event) {
        log.info("Processing activation event for user: {}", event.getUserEmail());
        accountActivationService.requestActivation(
                event.getUserExternalId(),
                event.getUserEmail(),
                event.getUserName(),
                event.getUserLastName());
    }

}
