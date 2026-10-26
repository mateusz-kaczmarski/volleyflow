package pl.volleyflow.auth.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.volleyflow.auth.event.AccountActivatedEvent;

@Service
@RequiredArgsConstructor
@Log4j2
public class AccountActivatedListener {

    private static final String USER_ACTIVE_KEY = "activation:user:";

    private final StringRedisTemplate redisTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAccountActivated(AccountActivatedEvent event) {
        log.info("Processing event for user: {}", event.userExternalId());
        try {
            redisTemplate.delete(USER_ACTIVE_KEY + event.userExternalId());
        } catch (Exception e) {
            log.error("Cannot delete redis key for user: {}", event.userExternalId(), e);
        }
    }

}
