package pl.volleyflow.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.volleyflow.auth.controller.UserLoginRequest;
import pl.volleyflow.auth.dto.AuthResponse;
import pl.volleyflow.auth.exceptions.InvalidCredentialsException;
import pl.volleyflow.personprofile.model.PersonProfileRequest;
import pl.volleyflow.personprofile.service.PersonProfileService;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.entity.UserAccountStatus;
import pl.volleyflow.user.model.UserAccountDto;
import pl.volleyflow.user.model.UserAccountRequest;
import pl.volleyflow.user.model.UserRegisterRequest;
import pl.volleyflow.user.service.UserAccountService;

@Service("authService")
@RequiredArgsConstructor
@Log4j2
public class AuthServiceImpl implements AuthService {

    private final UserAccountService userAccountService;
    private final PersonProfileService personProfileService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse register(UserRegisterRequest request) {
        UserAccountRequest userRequest = getUserAccountRequest(request);

        UserAccountDto userDto = userAccountService.create(userRequest);
        UserAccount userAccount = userAccountService.findByEmail(userDto.email())
                .orElseThrow(() -> new InvalidCredentialsException("User account was not created correctly"));
        personProfileService.createProfile(userAccount, getPersonProfileRequest(request));

        log.info("User registered: {}", userDto.email());

        //todo JWT service
        return AuthResponse.success(userDto.externalId());
    }

    @Override
    public AuthResponse login(UserLoginRequest request) {
        UserAccount userAccount = userAccountService.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        boolean isPasswordCorrect = passwordEncoder.matches(request.password(), userAccount.getPasswordHash());

        if (isPasswordCorrect
                && userAccount.isEmailVerified()
                && UserAccountStatus.ACTIVE.equals(userAccount.getStatus())) {
            //todo JWT service
            return AuthResponse.success(userAccount.getExternalId());
        }
        throw new InvalidCredentialsException("Invalid credentials");
    }

    private UserAccountRequest getUserAccountRequest(UserRegisterRequest request) {
        return new UserAccountRequest(
                request.email(),
                request.password(),
                request.phone()
        );
    }

    private PersonProfileRequest getPersonProfileRequest(UserRegisterRequest request) {
        return new PersonProfileRequest(
                request.firstName(),
                request.lastName(),
                request.displayName(),
                request.jumpCm()
        );
    }

}
