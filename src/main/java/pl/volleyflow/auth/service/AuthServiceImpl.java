package pl.volleyflow.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.volleyflow.auth.dto.AuthResponse;
import pl.volleyflow.auth.event.AccountActivatedEvent;
import pl.volleyflow.auth.event.UserRegisteredEvent;
import pl.volleyflow.auth.model.UserLoginRequest;
import pl.volleyflow.auth.model.exception.InvalidCredentialsException;
import pl.volleyflow.common.StringNormalizer;
import pl.volleyflow.common.TokenHashUtils;
import pl.volleyflow.personprofile.model.PersonProfile;
import pl.volleyflow.personprofile.model.PersonProfileCreateRequest;
import pl.volleyflow.personprofile.service.PersonProfileService;
import pl.volleyflow.security.JwtService;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.entity.UserAccountStatus;
import pl.volleyflow.user.model.UserAccountDto;
import pl.volleyflow.user.model.UserAccountRequest;
import pl.volleyflow.user.model.UserNotFoundException;
import pl.volleyflow.user.model.UserRegisterRequest;
import pl.volleyflow.user.service.UserAccountService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Service("authService")
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private static final String USER_ACTIVE_KEY = "activation:user:";

    private final UserAccountService userAccountService;
    private final PersonProfileService personProfileService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final StringRedisTemplate redisTemplate;

    @Override
    @Transactional
    public AuthResponse register(UserRegisterRequest request) {
        String normalizedEmail = StringNormalizer.normalizeEmail(request.email());
        log.info("Starting user registration for email {}", normalizedEmail);

        UserAccountRequest userRequest = getUserAccountRequest(request);

        UserAccountDto userDto = userAccountService.create(userRequest);

        UserAccount userAccount = userAccountService.findByEmail(userDto.email())
                .orElseThrow(() -> new InvalidCredentialsException("User account was not created correctly"));

        PersonProfile profile = personProfileService.createProfile(userAccount, getPersonProfileRequest(request));

        applicationEventPublisher.publishEvent(new UserRegisteredEvent(
                this,
                userAccount.getExternalId(),
                userAccount.getEmail(),
                profile.getFirstName(),
                profile.getLastName()));

        log.info("User registered successfully: email={}, externalId={}", userDto.email(), userDto.externalId());

        return AuthResponse.registerSuccess(userDto.externalId());
    }

    @Override
    @Transactional
    public AuthResponse login(UserLoginRequest request) {
        String normalizedEmail = StringNormalizer.normalizeEmail(request.email());
        log.info("Login attempt for email {}", normalizedEmail);

        UserAccount userAccount = userAccountService.findByEmail(normalizedEmail)
                .orElseThrow(() -> {
                    log.warn("Login failed for email {}: user not found", normalizedEmail);
                    return new InvalidCredentialsException("Invalid credentials");
                });

        boolean isPasswordCorrect = passwordEncoder.matches(request.password(), userAccount.getPasswordHash());

        if (isPasswordCorrect
                && userAccount.isEmailVerified()
                && UserAccountStatus.ACTIVE.equals(userAccount.getStatus())) {
            String token = jwtService.generateToken(userAccount);
            userAccount.setLastLoginAt(Instant.now());
            log.info("Login successful for email {}, externalId={}", normalizedEmail, userAccount.getExternalId());
            return AuthResponse.loginSuccess(token, userAccount.getExternalId());
        }
        log.warn("Login failed for email {}: invalid password or inactive account", normalizedEmail);
        throw new InvalidCredentialsException("Invalid credentials");
    }

    @Transactional
    @Override
    public AuthResponse active(UUID userExternalId, String token) {
        if (userExternalId == null || token == null) {
            return AuthResponse.activeFailed();
        }
        UserAccount userAccount = userAccountService.findByExternalId(userExternalId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        String key = USER_ACTIVE_KEY + userExternalId;
        String storedTokenHash = redisTemplate.opsForValue().get(key);

        if (storedTokenHash == null || token.isBlank()) {
            throw new InvalidCredentialsException("Activation link is invalid or expired");
        }

        byte[] receivedTokenHash = TokenHashUtils.generateHashForToken(token);
        byte[] expectedTokenHash = HexFormat.of().parseHex(storedTokenHash);

        if (!MessageDigest.isEqual(expectedTokenHash, receivedTokenHash)) {
            throw new InvalidCredentialsException("Activation link is invalid or expired");
        }

        if (UserAccountStatus.PENDING == userAccount.getStatus()) {
            userAccount.setStatus(UserAccountStatus.ACTIVE);
            userAccount.setEmailVerified(true);
            userAccount.setUpdatedAt(Instant.now());

            applicationEventPublisher.publishEvent(new AccountActivatedEvent(userExternalId));

            return AuthResponse.activeSuccessfully(null, userExternalId);
        }
        throw new InvalidCredentialsException("Account is not pending activation");
    }

    private UserAccountRequest getUserAccountRequest(UserRegisterRequest request) {
        return new UserAccountRequest(
                StringNormalizer.normalizeEmail(request.email()),
                request.password(),
                request.phone());
    }

    private PersonProfileCreateRequest getPersonProfileRequest(UserRegisterRequest request) {
        return new PersonProfileCreateRequest(
                request.firstName(),
                request.lastName(),
                request.displayName(),
                request.jumpCm());
    }

}
