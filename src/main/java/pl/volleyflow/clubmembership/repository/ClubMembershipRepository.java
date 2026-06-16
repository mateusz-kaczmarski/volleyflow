package pl.volleyflow.clubmembership.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.volleyflow.clubmembership.model.ClubMembership;
import pl.volleyflow.clubmembership.model.ClubMembershipRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClubMembershipRepository extends JpaRepository<ClubMembership, Long> {

    @Query(value = """
            select count(*) > 0
            from app.club_membership cm
            join app.club c on c.id = cm.club_id
            join app.person_profile pp on pp.id = cm.person_profile_id
            where c.external_id = :clubExternalId
              and pp.first_name = :firstName
              and pp.last_name = :lastName
              and cm.season = :season
            """, nativeQuery = true)
    boolean existsByClubExternalIdAndPersonProfileFirstNameAndPersonProfileLastNameAndSeason(
            @Param("clubExternalId") UUID clubExternalId,
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("season") String season
    );

    @Query(value = """
            select count(*) > 0
            from app.club_membership cm
            join app.club c on c.id = cm.club_id
            join app.person_profile pp on pp.id = cm.person_profile_id
            join app.user_account ua on ua.id = pp.user_account_id
            where c.external_id = :clubExternalId
              and ua.external_id = :userAccountExternalId
            """, nativeQuery = true)
    boolean existsByClubExternalIdAndPersonProfileUserAccountExternalId(
            @Param("clubExternalId") UUID clubExternalId,
            @Param("userAccountExternalId") UUID userAccountExternalId
    );

    @Query(value = """
            select count(*) > 0
            from app.club_membership cm
            join app.club c on c.id = cm.club_id
            join app.person_profile pp on pp.id = cm.person_profile_id
            join app.user_account ua on ua.id = pp.user_account_id
            where c.external_id = :clubExternalId
              and ua.email = :email
            """, nativeQuery = true)
    boolean existsByClubExternalIdAndPersonProfileUserAccountEmail(
            @Param("clubExternalId") UUID clubExternalId,
            @Param("email") String email
    );

    @Query(value = """
            select cm.*
            from app.club_membership cm
            join app.club c on c.id = cm.club_id
            join app.person_profile pp on pp.id = cm.person_profile_id
            join app.user_account ua on ua.id = pp.user_account_id
            where ua.external_id = :userAccountExternalId
              and c.active = true
            """, nativeQuery = true)
    List<ClubMembership> findAllByPersonProfileUserAccountExternalIdAndClubActiveTrue(
            @Param("userAccountExternalId") UUID userAccountExternalId
    );

    @Query(value = """
            select cm.*
            from app.club_membership cm
            join app.club c on c.id = cm.club_id
            where c.external_id = :clubExternalId
              and cm.role = :role
              and c.active = true
            """, nativeQuery = true)
    List<ClubMembership> findAllByClubExternalIdAndRoleAndClubActiveTrue(
            @Param("clubExternalId") UUID clubExternalId,
            @Param("role") ClubMembershipRole role
    );

    @Query(value = """
            select cm.*
            from app.club_membership cm
            join app.club c on c.id = cm.club_id
            where c.external_id = :clubExternalId
              and cm.role = 'PLAYER'
              and cm.active = true
              and c.active = true
            order by cm.shirt_number nulls last, cm.id
            """, nativeQuery = true)
    List<ClubMembership> findAllPlayersByClubExternalId(@Param("clubExternalId") UUID clubExternalId);

    @Query(value = """
            select cm.role
            from app.club_membership cm
            join app.club c on c.id = cm.club_id
            join app.person_profile pp on pp.id = cm.person_profile_id
            join app.user_account ua on ua.id = pp.user_account_id
            where c.external_id = :clubExternalId
              and ua.email = :email
              and c.active = true
            limit 1
            """, nativeQuery = true)
    Optional<String> findRoleByClubExternalIdAndUserEmail(
            @Param("clubExternalId") UUID clubExternalId,
            @Param("email") String email
    );

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
