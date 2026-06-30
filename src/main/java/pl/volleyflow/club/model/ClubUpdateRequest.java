package pl.volleyflow.club.model;

import jakarta.validation.constraints.Size;

public record ClubUpdateRequest(
        @Size(max = 120, message = "club name cannot exceed 120 characters")
        String name,

        @Size(max = 255, message = "avatar cannot exceed 255 characters")
        String avatar,

        @Size(max = 1024, message = "description cannot exceed 1024 characters")
        String description
) {
}
