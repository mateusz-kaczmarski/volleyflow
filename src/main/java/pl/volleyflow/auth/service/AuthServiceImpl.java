package pl.volleyflow.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pl.volleyflow.auth.dto.AuthResponse;
import pl.volleyflow.user.model.UserRegisterRequest;
import pl.volleyflow.user.model.UserAccountDto;
import pl.volleyflow.user.model.UserAccountRequest;
import pl.volleyflow.user.service.UserAccountService;

@Service("authService")
@RequiredArgsConstructor
@Log4j2
public class AuthServiceImpl implements AuthService {

    private final UserAccountService userAccountService;

    @Override
    public AuthResponse register(UserRegisterRequest request) {
        UserAccountRequest userRequest = getUserAccountRequest(request);

        UserAccountDto userDto = userAccountService.createUser(userRequest);

        log.info("User registered: {}", userDto.email());

        return AuthResponse.builder()
                .externalId(userDto.externalId())
                .email(userDto.email())
                .message("User registered successfully")
                .build();
    }

    private UserAccountRequest getUserAccountRequest(UserRegisterRequest request) {
        return new UserAccountRequest(
                request.email(),
                request.password(),
                request.phone()
        );
    }

}
