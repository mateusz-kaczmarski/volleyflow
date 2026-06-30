package pl.volleyflow.user.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record UserAccountRequest(
        @NotBlank(message = "email cannot be blank")
        String email,
        @NotBlank(message = "password cannot be blank")
        String password,
        String phone) {
}
