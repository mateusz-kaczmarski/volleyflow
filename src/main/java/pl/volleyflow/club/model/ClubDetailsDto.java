package pl.volleyflow.club.model;

import lombok.Builder;
import pl.volleyflow.clubmembership.model.ClubMemberDto;

import java.util.List;
import java.util.UUID;

@Builder
public record ClubDetailsDto(
        UUID externalId,
        String name,
        String avatar,
        String description,
        String userRole,
        List<ClubMemberDto> members) {
}