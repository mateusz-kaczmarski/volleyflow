package pl.volleyflow.clubmembership.model;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

public record ClubMembershipCreateRequest(
        @NotNull(message = "club externalId cannot be null")
        UUID clubExternalId,

        @NotNull(message = "role cannot be null")
        ClubMembershipRole role,

        @NotBlank(message = "first name cannot be blank")
        @Size(max = 80, message = "first name cannot exceed 80 characters")
        String firstName,

        @NotBlank(message = "last name cannot be blank")
        @Size(max = 80, message = "last name cannot exceed 80 characters")
        String lastName,

        @Size(max = 120, message = "display name cannot exceed 120 characters")
        String displayName,

        @Min(value = 1, message = "shirt number must be greater than 0")
        @Max(value = 99, message = "shirt number must be less than 100")
        Integer shirtNumber,

        @NotEmpty(message = "positions cannot be empty")
        Set<MemberPosition> positions,

        LocalDate activeFrom,
        LocalDate activeTo) {
}
