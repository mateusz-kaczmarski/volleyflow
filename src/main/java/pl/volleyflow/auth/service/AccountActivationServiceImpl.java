package pl.volleyflow.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import pl.volleyflow.auth.event.AccountActivationEmailRequested;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class AccountActivationServiceImpl implements AccountActivationService {

    private static final String USER_ACTIVE_KEY = "activation:user:";

    private final StringRedisTemplate redisTemplate;
    private final KafkaTemplate<String, AccountActivationEmailRequested> kafkaTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.kafka.topics.user-account-created}")
    private String userCreatedTopic;


    @Override
    public void requestActivation(UUID userExternalId, String email, String name, String lastName) {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        byte[] hashBytes = generateHashForToken(token);
        String tokenHash = HexFormat.of().formatHex(hashBytes);

        String key = USER_ACTIVE_KEY + userExternalId;
        redisTemplate.opsForValue().set(key, tokenHash, Duration.ofMinutes(15));

        AccountActivationEmailRequested event = AccountActivationEmailRequested.builder()
                .eventId(UUID.randomUUID())
                .userExternalId(userExternalId)
                .email(email)
                .firstName(name)
                .lastName(lastName)
                .activationToken(token)
                .build();

        try {
            sendEventToKafka(userExternalId, event);
        } catch (Exception exception) {
            log.error(
                    "Could not initiate activation email publication: eventId={}, userExternalId={}",
                    event.eventId(),
                    userExternalId,
                    exception
            );
        }
    }

    private void sendEventToKafka(UUID userExternalId, AccountActivationEmailRequested event) {
        kafkaTemplate.send(userCreatedTopic, userExternalId.toString(), event)
                .whenComplete((result, exception) -> {
                    if (exception != null) {
                        log.error(
                                "Failed to publish activation email request: eventId={}, userExternalId={}",
                                event.eventId(),
                                userExternalId,
                                exception
                        );
                        return;
                    }

                    log.info(
                            "Published activation email request: eventId={}, userExternalId={}",
                            event.eventId(),
                            userExternalId
                    );
                });
    }

    private static byte[] generateHashForToken(String token) {
        try {
            return MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

}
