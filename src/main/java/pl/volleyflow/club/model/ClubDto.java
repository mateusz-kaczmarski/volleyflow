package pl.volleyflow.club.model;

import lombok.Builder;

import java.util.UUID;

@Builder
public record ClubDto(
        UUID externalId,
        String name,
        String avatar,
        String description,
        String userRole) {
}
