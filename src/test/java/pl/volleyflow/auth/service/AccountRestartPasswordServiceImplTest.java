package pl.volleyflow.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import pl.volleyflow.auth.event.AccountRestartPasswordRequested;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AccountRestartPasswordServiceImplTest {

    @Mock
    private KafkaTemplate<String, AccountRestartPasswordRequested> kafkaTemplate;

    @InjectMocks
    private AccountRestartPasswordServiceImpl passwordService;

    @Test
    void shouldPublishPasswordResetRequest() {
        UUID userId = UUID.randomUUID();
        ReflectionTestUtils.setField(passwordService, "userRestartPasswordTopic", "reset-topic");

        passwordService.restartPasswordRequest(userId, "user@example.com", "token");

        var event = org.mockito.ArgumentCaptor.forClass(AccountRestartPasswordRequested.class);
        verify(kafkaTemplate).send(eq("reset-topic"), event.capture());
        assertThat(event.getValue().userAccountExternalId()).isEqualTo(userId);
        assertThat(event.getValue().token()).isEqualTo("token");
    }
}
