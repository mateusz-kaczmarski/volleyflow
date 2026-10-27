package pl.volleyflow.auth.service;

import pl.volleyflow.auth.dto.AuthResponse;
import pl.volleyflow.auth.model.UserLoginRequest;
import pl.volleyflow.user.model.UserRegisterRequest;

import java.util.UUID;

public interface AuthService {

    AuthResponse register(UserRegisterRequest request);

    AuthResponse login(UserLoginRequest userLoginRequest);

    AuthResponse active(UUID userExternalId, String token);

    AuthResponse restartPassword(String email);

}
