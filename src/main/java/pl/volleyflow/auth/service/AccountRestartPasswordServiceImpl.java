package pl.volleyflow.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import pl.volleyflow.auth.event.AccountRestartPasswordRequested;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountRestartPasswordServiceImpl implements AccountRestartPasswordService {

    private final KafkaTemplate<String, AccountRestartPasswordRequested> kafkaTemplate;

    @Value("${app.kafka.topics.user-password-restart}")
    private String userRestartPasswordTopic;

    @Override
    public void restartPasswordRequest(UUID userAccountExternalId, String email, String token) {
        AccountRestartPasswordRequested accountRestartPasswordRequested =
                new AccountRestartPasswordRequested(UUID.randomUUID(), userAccountExternalId, email, token);

        kafkaTemplate.send(userRestartPasswordTopic, accountRestartPasswordRequested);
    }

}
