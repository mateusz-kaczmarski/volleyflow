package pl.volleyflow.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.volleyflow.auth.controller.UserLoginRequest;
import pl.volleyflow.auth.dto.AuthResponse;
import pl.volleyflow.auth.exceptions.InvalidCredentialsException;
import pl.volleyflow.personprofile.model.PersonProfileCreateRequest;
import pl.volleyflow.personprofile.service.PersonProfileService;
import pl.volleyflow.security.JwtService;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.entity.UserAccountStatus;
import pl.volleyflow.user.model.UserAccountDto;
import pl.volleyflow.user.model.UserAccountRequest;
import pl.volleyflow.user.model.UserRegisterRequest;
import pl.volleyflow.user.service.UserAccountService;

import java.util.Locale;

@Service("authService")
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserAccountService userAccountService;
    private final PersonProfileService personProfileService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional
    public AuthResponse register(UserRegisterRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        log.info("Starting user registration for email {}", normalizedEmail);

        UserAccountRequest userRequest = getUserAccountRequest(request);

        UserAccountDto userDto = userAccountService.create(userRequest);
        UserAccount userAccount = userAccountService.findByEmail(userDto.email())
                .orElseThrow(() -> new InvalidCredentialsException("User account was not created correctly"));
        personProfileService.createProfile(userAccount, getPersonProfileRequest(request));
        String token = jwtService.generateToken(userAccount);

        log.info("User registered successfully: email={}, externalId={}", userDto.email(), userDto.externalId());

        return AuthResponse.registerSuccess(token, userDto.externalId());
    }

    @Override
    public AuthResponse login(UserLoginRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
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
            log.info("Login successful for email {}, externalId={}", normalizedEmail, userAccount.getExternalId());
            return AuthResponse.loginSuccess(token, userAccount.getExternalId());
        }
        log.warn("Login failed for email {}: invalid password or inactive account", normalizedEmail);
        throw new InvalidCredentialsException("Invalid credentials");
    }

    private UserAccountRequest getUserAccountRequest(UserRegisterRequest request) {
        return new UserAccountRequest(
                normalizeEmail(request.email()),
                request.password(),
                request.phone()
        );
    }

    private PersonProfileCreateRequest getPersonProfileRequest(UserRegisterRequest request) {
        return new PersonProfileCreateRequest(
                request.firstName(),
                request.lastName(),
                request.displayName(),
                request.jumpCm()
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

}
