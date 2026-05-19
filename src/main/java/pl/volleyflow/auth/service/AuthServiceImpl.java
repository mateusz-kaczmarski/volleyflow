package pl.volleyflow.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.volleyflow.auth.controller.UserLoginRequest;
import pl.volleyflow.auth.dto.AuthResponse;
import pl.volleyflow.auth.exceptions.InvalidCredentialsException;
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
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthResponse register(UserRegisterRequest request) {
        UserAccountRequest userRequest = getUserAccountRequest(request);

        UserAccountDto userDto = userAccountService.createUser(userRequest);

        log.info("User registered: {}", userDto.email());

        //todo JWT service
        return AuthResponse.builder()
                .build();
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
            return new AuthResponse(null, "Login successfully");
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

}
