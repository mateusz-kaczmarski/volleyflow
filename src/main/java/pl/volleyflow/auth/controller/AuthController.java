package pl.volleyflow.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.volleyflow.auth.dto.AuthResponse;
import pl.volleyflow.auth.model.UserLoginRequest;
import pl.volleyflow.auth.model.UserRestartPasswordRequest;
import pl.volleyflow.auth.service.AuthService;
import pl.volleyflow.user.model.UserRegisterRequest;

import java.util.UUID;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Log4j2
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid UserRegisterRequest request) {
        log.info("Registration request for email: {}", request.email());
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid UserLoginRequest userLoginRequest) {
        log.info("Login request for email: {}", userLoginRequest.email());
        return ResponseEntity.ok(authService.login(userLoginRequest));
    }

    @PostMapping("/activate")
    public ResponseEntity<AuthResponse> active(@RequestParam UUID userExternalId,
                                               @RequestParam String token) {
        return ResponseEntity.ok(authService.active(userExternalId, token));
    }

    @PostMapping("/password-reset")
    public ResponseEntity<AuthResponse> resetPassword(@RequestBody @Valid UserRestartPasswordRequest request) {
        return ResponseEntity.ok(authService.restartPassword(request.email()));
    }

}
