package pl.volleyflow.auth.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.volleyflow.auth.dto.AuthResponse;
import pl.volleyflow.auth.service.AuthServiceImpl;
import pl.volleyflow.user.model.UserRegisterRequest;

@RestController
@RequestMapping("/internal/auth")
@RequiredArgsConstructor
@Log4j2
public class AuthController {

    private final AuthServiceImpl authServiceImpl;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody UserRegisterRequest request) {
        log.info("Registration request for email: {}", request.email());
        AuthResponse response = authServiceImpl.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
