package pl.volleyflow.auth.controller;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public record UserLoginRequest(
        @NotBlank(message = "email cannot be empty")
        @Email(message = "invalid email format")
        String email,
        @NotBlank(message = "password cannot be empty")
        String password) {
}
