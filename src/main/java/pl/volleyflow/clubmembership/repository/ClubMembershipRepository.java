package pl.volleyflow.clubmembership.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.volleyflow.clubmembership.model.ClubMembership;

import java.util.List;
import java.util.UUID;

public interface ClubMembershipRepository extends JpaRepository<ClubMembership, Long> {

    boolean existsByClub_ExternalIdAndPersonProfile_FirstNameAndPersonProfile_LastNameAndSeason(
            UUID clubExternalId,
            String firstName,
            String lastName,
            String season
    );

    boolean existsByClub_ExternalIdAndPersonProfile_UserAccount_ExternalId(
            UUID clubExternalId,
            UUID userAccountExternalId
    );

    List<ClubMembership> findAllByPersonProfile_UserAccount_ExternalId(UUID userAccountExternalId);
}
