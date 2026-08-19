package pl.volleyflow.match.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record MatchLocation(
        @NotBlank(message = "city cannot be blank")
        @Size(max = 80, message = "city cannot exceed 80 characters")
        String city,

        @NotBlank(message = "zip code cannot be blank")
        @Pattern(regexp = "\\d{2}-\\d{3}", message = "zip code must use format XX-XXX")
        String zipCode,

        @NotBlank(message = "street cannot be blank")
        @Size(max = 120, message = "street cannot exceed 120 characters")
        String street,

        @NotBlank(message = "building number cannot be blank")
        @Size(max = 20, message = "building number cannot exceed 20 characters")
        String buildingNumber) {
}
