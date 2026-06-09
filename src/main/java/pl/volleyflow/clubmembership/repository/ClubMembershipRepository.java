package pl.volleyflow.clubmembership.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.volleyflow.clubmembership.model.ClubMembership;
import pl.volleyflow.clubmembership.model.ClubMembershipRole;

import java.util.List;
import java.util.UUID;

public interface ClubMembershipRepository extends JpaRepository<ClubMembership, Long> {

    boolean existsByClubExternalIdAndPersonProfileFirstNameAndPersonProfileLastNameAndSeason(
            UUID clubExternalId,
            String firstName,
            String lastName,
            String season
    );

    boolean existsByClubExternalIdAndPersonProfileUserAccountExternalId(
            UUID clubExternalId,
            UUID userAccountExternalId
    );

    boolean existsByClubExternalIdAndPersonProfileUserAccountEmail(UUID clubExternalId, String email);

    List<ClubMembership> findAllByPersonProfileUserAccountExternalId(UUID userAccountExternalId);

    List<ClubMembership> findAllByClubExternalIdAndRole(UUID clubExternalId, ClubMembershipRole role);
}
