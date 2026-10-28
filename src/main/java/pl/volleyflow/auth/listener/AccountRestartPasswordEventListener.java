package pl.volleyflow.auth.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import pl.volleyflow.auth.event.AccountRestartPasswordEvent;
import pl.volleyflow.auth.service.AccountRestartPasswordService;

@Component
@RequiredArgsConstructor
@Log4j2
public class AccountRestartPasswordEventListener {

    private final AccountRestartPasswordService accountRestartPasswordService;

    @EventListener
    public void sendRestartPasswordEmail(AccountRestartPasswordEvent event) {
        log.info("Received event for restart password");
        accountRestartPasswordService.restartPasswordRequest(
                event.userAccountExternalId(),
                event.email(),
                event.token());
    }

}
