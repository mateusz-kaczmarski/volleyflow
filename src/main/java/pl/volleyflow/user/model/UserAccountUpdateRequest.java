package pl.volleyflow.user.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UserAccountUpdateRequest(
        @Email(message = "invalid email format")
        @Size(max = 255, message = "email cannot exceed 255 characters")
        String email,

        @Size(max = 255, message = "phone cannot exceed 255 characters")
        String phone) {
}
