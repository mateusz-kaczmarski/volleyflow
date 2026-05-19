package pl.volleyflow.user.model;

import lombok.Builder;

import javax.validation.constraints.NotBlank;

@Builder
public record UserAccountRequest(
        @NotBlank(message = "email cannot be blank")
        String email,
        @NotBlank(message = "password cannot be blank")
        String password,
        String phone) {
}
