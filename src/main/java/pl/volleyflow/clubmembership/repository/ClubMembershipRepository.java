package pl.volleyflow.clubmembership.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query(value = """
            select case
                     when count(*) > 0 then true
                     else false
                   end
            from app.club_membership cm
            join app.person_profile pp on pp.id = cm.person_profile_id
            where pp.user_account_id = :userId
              and cm.club_id = :clubId
              and cm.role = 'OWNER'
            """, nativeQuery = true)
    boolean isOwnerClub(@Param("userId") long userId, @Param("clubId") long clubId);
}
