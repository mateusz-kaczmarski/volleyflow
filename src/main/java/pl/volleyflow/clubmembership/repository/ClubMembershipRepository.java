package pl.volleyflow.clubmembership.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.volleyflow.clubmembership.model.ClubMembership;

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
    boolean existsPlayerInClubSeason(@Param("clubExternalId") UUID clubExternalId,
                                     @Param("firstName") String firstName,
                                     @Param("lastName") String lastName,
                                     @Param("season") String season);

    @Query(value = """
            select count(*) > 0
            from app.club_membership cm
            join app.club c on c.id = cm.club_id
            join app.person_profile pp on pp.id = cm.person_profile_id
            join app.user_account ua on ua.id = pp.user_account_id
            where c.external_id = :clubExternalId
              and ua.external_id = :userAccountExternalId
            """, nativeQuery = true)
    boolean existsUserMembershipInClub(@Param("clubExternalId") UUID clubExternalId,
                                       @Param("userAccountExternalId") UUID userAccountExternalId);

    @Query(value = """
            select count(*) > 0
            from app.club_membership cm
            join app.club c on c.id = cm.club_id
            join app.person_profile pp on pp.id = cm.person_profile_id
            join app.user_account ua on ua.id = pp.user_account_id
            where c.external_id = :clubExternalId
              and ua.email = :email
            """, nativeQuery = true)
    boolean isClubMember(@Param("clubExternalId") UUID clubExternalId,
                         @Param("email") String email);

    @Query(value = """
            select cm.*
            from app.club_membership cm
            join app.club c on c.id = cm.club_id
            join app.person_profile pp on pp.id = cm.person_profile_id
            join app.user_account ua on ua.id = pp.user_account_id
            where ua.external_id = :userAccountExternalId
              and c.active = true
            """, nativeQuery = true)
    List<ClubMembership> findActiveClubMembershipsByUserExternalId(@Param("userAccountExternalId") UUID userAccountExternalId);

    @Query(value = """
            select cm.*
            from app.club_membership cm
            join app.club c on c.id = cm.club_id
            where c.external_id = :clubExternalId
              and cm.role = :role
              and (:active is null or cm.active = :active)
              and c.active = true
            order by cm.shirt_number nulls last, cm.id
            """, nativeQuery = true)
    List<ClubMembership> findMembershipsByClubRoleAndActiveFilter(@Param("clubExternalId") UUID clubExternalId,
                                                                  @Param("role") String role,
                                                                  @Param("active") Boolean active);

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
    List<ClubMembership> findActivePlayersByClubExternalId(@Param("clubExternalId") UUID clubExternalId);

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
    Optional<String> findRoleByClubExternalIdAndUserEmail(@Param("clubExternalId") UUID clubExternalId,
                                                          @Param("email") String email);

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
    boolean isClubOwner(@Param("userId") long userId, @Param("clubId") long clubId);

    @Query(value = """
        select cm.*
        from app.club_membership cm
        join app.club c on c.id = cm.club_id
        where c.external_id = :clubExternalId
          and cm.external_id = :memberExternalId
          and c.active = true
        """, nativeQuery = true)
    Optional<ClubMembership> findMembershipByClubExternalIdAndMembershipExternalId(@Param("clubExternalId") UUID clubExternalId,
                                                                                   @Param("memberExternalId") UUID memberExternalId);

    @Query(value = """
            select count(*) > 0
            from app.club_membership cm
            join app.club c on c.id = cm.club_id
            join app.person_profile pp on pp.id = cm.person_profile_id
            join app.user_account ua on ua.id = pp.user_account_id
            where c.external_id = :clubExternalId
              and ua.email = :email
              and cm.role in ('OWNER', 'TRAINER')
              and cm.active = true
              and c.active = true
            """, nativeQuery = true)
    boolean canManageClubMemberships(@Param("clubExternalId") UUID clubExternalId,
                                     @Param("email") String email);

    @Modifying
    @Query(value = """
            update app.club_membership cm
            set active = false,
                updated_at = now()
            from app.club c
            where c.id = cm.club_id
              and c.external_id = :clubExternalId
              and cm.external_id = :membershipExternalId
              and c.active = true
            """, nativeQuery = true)
    int deactivateByClubExternalIdAndMembershipExternalId(@Param("clubExternalId") UUID clubExternalId,
                                                          @Param("membershipExternalId") UUID membershipExternalId);

}
