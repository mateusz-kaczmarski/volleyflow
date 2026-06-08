package pl.volleyflow.club.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public record ClubRequest(
        @NotBlank(message = "club name cannot be blank")
        @Size(max = 120, message = "club name cannot exceed 120 characters")
        String name,
        @Size(max = 255, message = "avatar cannot exceed 20 characters")
        String avatar,
        @Size(max = 1024, message = "description cannot exceed 1024 characters")
        String description) {
}
