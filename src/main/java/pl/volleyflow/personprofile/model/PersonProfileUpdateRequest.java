package pl.volleyflow.personprofile.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record PersonProfileUpdateRequest(
        @Size(max = 80, message = "first name cannot exceed 80 characters")
        String firstName,

        @Size(max = 80, message = "last name cannot exceed 80 characters")
        String lastName,

        @Size(max = 120, message = "display name cannot exceed 120 characters")
        String displayName,

        @Min(value = 0, message = "jump must be positive")
        @Max(value = 200, message = "jump must be less than 200 cm")
        Integer jumpCm) {
}
