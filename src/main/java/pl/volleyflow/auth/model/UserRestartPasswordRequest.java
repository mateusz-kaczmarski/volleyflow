package pl.volleyflow.auth.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserRestartPasswordRequest(
        @Email(message = "wrong email")
        @NotBlank(message = "email cannot be empty")
        String email) {
}
