package pl.volleyflow.auth.dto;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank(message = "email cannot be blank")
        @Email(message = "invalid email format")
        String email,
        @NotBlank(message = "password cannot be blank")
        String password) {
}
