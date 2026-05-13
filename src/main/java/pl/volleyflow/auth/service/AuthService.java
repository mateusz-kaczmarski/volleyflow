package pl.volleyflow.auth.service;

import pl.volleyflow.auth.dto.AuthResponse;
import pl.volleyflow.user.model.UserRegisterRequest;

public interface AuthService {

    AuthResponse register(UserRegisterRequest request);

}
