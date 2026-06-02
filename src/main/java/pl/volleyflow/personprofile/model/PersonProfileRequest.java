package pl.volleyflow.personprofile.model;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public record PersonProfileRequest(
        @NotBlank(message = "first name cannot be blank")
        @Size(max = 80, message = "first name cannot exceed 80 characters")
        String firstName,

        @NotBlank(message = "last name cannot be blank")
        @Size(max = 80, message = "last name cannot exceed 80 characters")
        String lastName,

        @Size(max = 120, message = "display name cannot exceed 120 characters")
        String displayName,

        @Min(value = 0, message = "jump must be positive")
        @Max(value = 200, message = "jump must be less than 200 cm")
        Integer jumpCm) {
}

