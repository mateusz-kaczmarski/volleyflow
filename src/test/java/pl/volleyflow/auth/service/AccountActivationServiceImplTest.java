package pl.volleyflow.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import pl.volleyflow.auth.event.AccountActivationEmailRequested;
import pl.volleyflow.common.TokenHashUtils;

import java.time.Duration;
import java.util.HexFormat;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountActivationServiceImplTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> values;
    @Mock
    private KafkaTemplate<String, AccountActivationEmailRequested> kafkaTemplate;

    @InjectMocks
    private AccountActivationServiceImpl activationService;

    @Test
    void shouldStoreTokenHashAndSendActivationEvent() {
        UUID userId = UUID.randomUUID();
        ReflectionTestUtils.setField(activationService, "userCreatedTopic", "activation-topic");
        when(redisTemplate.opsForValue()).thenReturn(values);
        when(kafkaTemplate.send(eq("activation-topic"), eq(userId.toString()), any()))
                .thenReturn(CompletableFuture.completedFuture(null));

        activationService.requestActivation(userId, "user@example.com", "Jan", "Kowalski");

        var event = org.mockito.ArgumentCaptor.forClass(AccountActivationEmailRequested.class);
        verify(kafkaTemplate).send(eq("activation-topic"), eq(userId.toString()), event.capture());
        assertThat(event.getValue().email()).isEqualTo("user@example.com");
        verify(values).set("activation:user:" + userId,
                HexFormat.of().formatHex(TokenHashUtils.generateHashForToken(event.getValue().activationToken())),
                Duration.ofMinutes(15));
    }
}
