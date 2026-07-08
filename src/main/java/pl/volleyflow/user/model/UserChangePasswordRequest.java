package pl.volleyflow.user.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserChangePasswordRequest(
        @NotBlank(message = "old password cannot be blank")
        String oldPassword,

        @NotBlank(message = "new password cannot be blank")
        @Size(min = 6, message = "new password must be at least 6 characters")
        String newPassword) {
}
