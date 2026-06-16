package pl.volleyflow.clubmembership.model;

import lombok.Builder;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Builder
public record ClubMemberDto(
        UUID externalId,
        UUID personProfileExternalId,
        ClubMembershipRole role,
        String firstName,
        String lastName,
        String displayName,
        Integer shirtNumber,
        String season,
        Set<MemberPosition> positions,
        LocalDate activeFrom,
        LocalDate activeTo,
        boolean active) {
}
