package pl.volleyflow.auth.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.volleyflow.auth.event.AccountActivatedEvent;
import pl.volleyflow.auth.event.UserRegisteredEvent;
import pl.volleyflow.auth.model.ConfirmPasswordRequest;
import pl.volleyflow.auth.model.UserLoginRequest;
import pl.volleyflow.auth.model.exception.InvalidCredentialsException;
import pl.volleyflow.common.TokenHashUtils;
import pl.volleyflow.personprofile.model.PersonProfile;
import pl.volleyflow.personprofile.service.PersonProfileService;
import pl.volleyflow.security.JwtService;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.entity.UserAccountStatus;
import pl.volleyflow.user.model.UserAccountDto;
import pl.volleyflow.user.model.UserAccountRequest;
import pl.volleyflow.user.model.UserRegisterRequest;
import pl.volleyflow.user.service.UserAccountService;

import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    private static final String EMAIL = "user@example.com";
    private final UUID userId = UUID.randomUUID();

    @Mock
    private UserAccountService userAccountService;
    @Mock
    private PersonProfileService personProfileService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private StringRedisTemplate redisTemplate;
    @Mock
    private ValueOperations<String, String> values;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void shouldRegisterAccountAndPublishEvent() {
        UserRegisterRequest request = new UserRegisterRequest(" USER@EXAMPLE.COM ", "password123",
                "123456789", "Jan", "Kowalski", "Jan", 40);
        UserAccount user = UserAccount.builder().externalId(userId).email(EMAIL).build();
        when(userAccountService.create(any(UserAccountRequest.class)))
                .thenReturn(UserAccountDto.builder().externalId(userId).email(EMAIL).build());
        when(userAccountService.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(personProfileService.createProfile(eq(user), any()))
                .thenReturn(PersonProfile.builder().firstName("Jan").lastName("Kowalski").build());

        var result = authService.register(request);

        assertThat(result.externalId()).isEqualTo(userId);
        assertThat(result.token()).isNull();
        verify(userAccountService).create(new UserAccountRequest(EMAIL, "password123", "123456789"));
        verify(eventPublisher).publishEvent(any(UserRegisteredEvent.class));
    }

    @Test
    void shouldLoginActiveUser() {
        UserAccount user = UserAccount.builder().externalId(userId).email(EMAIL)
                .passwordHash("hash").status(UserAccountStatus.ACTIVE).emailVerified(true).build();
        when(userAccountService.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hash")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("jwt");

        var result = authService.login(new UserLoginRequest(" USER@EXAMPLE.COM ", "password"));

        assertThat(result.token()).isEqualTo("jwt");
        assertThat(user.getLastLoginAt()).isNotNull();
    }

    @Test
    void shouldRejectUnverifiedLogin() {
        UserAccount user = UserAccount.builder().passwordHash("hash")
                .status(UserAccountStatus.ACTIVE).emailVerified(false).build();
        when(userAccountService.findByEmail(EMAIL)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "hash")).thenReturn(true);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(new UserLoginRequest(EMAIL, "password")));
        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldActivatePendingAccountWithMatchingToken() {
        UserAccount user = UserAccount.builder().status(UserAccountStatus.PENDING).build();
        String token = "activation-token";
        when(userAccountService.findByExternalId(userId)).thenReturn(Optional.of(user));
        when(redisTemplate.opsForValue()).thenReturn(values);
        when(values.get("activation:user:" + userId))
                .thenReturn(HexFormat.of().formatHex(TokenHashUtils.generateHashForToken(token)));

        var result = authService.active(userId, token);

        assertThat(result.externalId()).isEqualTo(userId);
        assertThat(user.getStatus()).isEqualTo(UserAccountStatus.ACTIVE);
        assertThat(user.isEmailVerified()).isTrue();
        verify(eventPublisher).publishEvent(any(AccountActivatedEvent.class));
    }

    @Test
    void shouldRejectExpiredActivationToken() {
        when(userAccountService.findByExternalId(userId)).thenReturn(Optional.of(new UserAccount()));
        when(redisTemplate.opsForValue()).thenReturn(values);

        assertThrows(InvalidCredentialsException.class, () -> authService.active(userId, "token"));
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void shouldNotRevealWhetherResetEmailExists() {
        when(userAccountService.findByEmail(EMAIL)).thenReturn(Optional.empty());

        assertThat(authService.restartPassword(EMAIL).message()).isEqualTo("Send email for restart password");
        verifyNoInteractions(redisTemplate, eventPublisher);
    }

    @Test
    void shouldRejectExpiredPasswordResetToken() {
        when(redisTemplate.opsForValue()).thenReturn(values);

        assertThrows(InvalidCredentialsException.class,
                () -> authService.confirmRestartPassword(new ConfirmPasswordRequest("token", "newPassword")));
        verifyNoInteractions(passwordEncoder);
    }

    @Test
    void shouldChangePasswordWhenResetTokenIsValid() {
        String tokenHash = HexFormat.of().formatHex(TokenHashUtils.generateHashForToken("token"));
        String tokenKey = "restartPassword:token:" + tokenHash;
        String userKey = "restartPassword:user:" + userId;
        UserAccount user = UserAccount.builder().passwordHash("oldHash").build();
        when(redisTemplate.opsForValue()).thenReturn(values);
        when(values.get(tokenKey)).thenReturn(userId.toString());
        when(values.get(userKey)).thenReturn(tokenHash);
        when(userAccountService.findByExternalId(userId)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword")).thenReturn("newHash");
        when(values.getAndDelete(tokenKey)).thenReturn(userId.toString());

        authService.confirmRestartPassword(new ConfirmPasswordRequest("token", "newPassword"));

        assertThat(user.getPasswordHash()).isEqualTo("newHash");
        verify(values).getAndDelete(tokenKey);
    }
}
