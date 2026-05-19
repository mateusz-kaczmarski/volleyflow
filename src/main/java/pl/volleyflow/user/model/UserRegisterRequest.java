package pl.volleyflow.user.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public record UserRegisterRequest(
        @NotBlank(message = "email cannot be blank")
        @Email(message = "invalid email format")
        String email,
        @NotBlank(message = "password cannot be blank")
        @Size(min = 6, message = "password must be at least 6 characters")
        String password,
        String phone) {

}
