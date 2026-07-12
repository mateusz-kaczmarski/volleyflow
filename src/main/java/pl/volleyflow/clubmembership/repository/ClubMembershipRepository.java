package pl.volleyflow.clubmembership.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.volleyflow.club.model.ClubStatus;
import pl.volleyflow.clubmembership.model.ClubMembership;
import pl.volleyflow.clubmembership.model.ClubMembershipRole;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClubMembershipRepository extends JpaRepository<ClubMembership, Long> {

    @Query("""
            select count(cm) > 0
            from ClubMembership cm
            where cm.club.externalId = :clubId
              and cm.personProfile.firstName = :firstName
              and cm.personProfile.lastName = :lastName
              and cm.active = true
              and cm.club.clubStatus = :clubStatus
            """)
    boolean existsPlayer(@Param("clubId") UUID clubId,
                         @Param("firstName") String firstName,
                         @Param("lastName") String lastName,
                         @Param("clubStatus") ClubStatus clubStatus);

    @Query("""
            select count(cm) > 0
            from ClubMembership cm
            where cm.club.externalId = :clubId
              and cm.personProfile.firstName = :firstName
              and cm.personProfile.lastName = :lastName
              and cm.externalId <> :membershipId
              and cm.active = true
              and cm.club.clubStatus = :clubStatus
            """)
    boolean existsPlayerExcept(@Param("clubId") UUID clubId,
                               @Param("firstName") String firstName,
                               @Param("lastName") String lastName,
                               @Param("membershipId") UUID membershipId,
                               @Param("clubStatus") ClubStatus clubStatus);

    @Query("""
            select count(cm) > 0
            from ClubMembership cm
            where cm.club.externalId = :clubId
              and cm.shirtNumber = :shirtNumber
              and cm.active = true
              and cm.club.clubStatus = :clubStatus
            """)
    boolean existsShirtNumber(@Param("clubId") UUID clubId,
                              @Param("shirtNumber") Integer shirtNumber,
                              @Param("clubStatus") ClubStatus clubStatus);

    @Query("""
            select count(cm) > 0
            from ClubMembership cm
            where cm.club.externalId = :clubId
              and cm.shirtNumber = :shirtNumber
              and cm.externalId <> :membershipId
              and cm.active = true
              and cm.club.clubStatus = :clubStatus
            """)
    boolean existsShirtNumberExcept(@Param("clubId") UUID clubId,
                                    @Param("shirtNumber") Integer shirtNumber,
                                    @Param("membershipId") UUID membershipId,
                                    @Param("clubStatus") ClubStatus clubStatus);

    @Query("""
            select count(cm) > 0
            from ClubMembership cm
            where cm.club.externalId = :clubId
              and cm.personProfile.userAccount.externalId = :userId
              and cm.active = true
              and cm.club.clubStatus = :clubStatus
            """)
    boolean existsUserInClub(@Param("clubId") UUID clubId,
                              @Param("userId") UUID userId,
                              @Param("clubStatus") ClubStatus clubStatus);

    boolean existsByPersonProfileId(Long personProfileId);

    @Query("""
            select count(cm) > 0
            from ClubMembership cm
            where cm.club.externalId = :clubId
              and cm.personProfile.userAccount.email = :email
              and cm.active = true
              and cm.club.clubStatus = :clubStatus
            """)
    boolean isClubMember(@Param("clubId") UUID clubId,
                         @Param("email") String email,
                         @Param("clubStatus") ClubStatus clubStatus);

    @Query("""
            select cm
            from ClubMembership cm
            join fetch cm.club c
            where cm.personProfile.userAccount.externalId = :userId
              and cm.active = true
              and c.clubStatus = :clubStatus
            """)
    List<ClubMembership> findActiveByUser(@Param("userId") UUID userId,
                                          @Param("clubStatus") ClubStatus clubStatus);

    @Query("""
            select cm
            from ClubMembership cm
            join fetch cm.club c
            join fetch cm.personProfile pp
            where c.externalId = :clubId
              and cm.role = :role
              and (:active is null or cm.active = :active)
              and c.clubStatus = :clubStatus
            order by case when cm.shirtNumber is null then 1 else 0 end, cm.shirtNumber, cm.id
            """)
    List<ClubMembership> findByClubAndRole(@Param("clubId") UUID clubId,
                                           @Param("role") ClubMembershipRole role,
                                           @Param("active") Boolean active,
                                           @Param("clubStatus") ClubStatus clubStatus);

    @Query("""
            select cm
            from ClubMembership cm
            join fetch cm.personProfile pp
            where cm.club.externalId = :clubId
              and cm.role = :role
              and cm.active = true
              and cm.club.clubStatus = :clubStatus
            order by case when cm.shirtNumber is null then 1 else 0 end, cm.shirtNumber, cm.id
            """)
    List<ClubMembership> findActiveByRole(@Param("clubId") UUID clubId,
                                          @Param("role") ClubMembershipRole role,
                                          @Param("clubStatus") ClubStatus clubStatus);

    @Query("""
            select cm.role
            from ClubMembership cm
            where cm.club.externalId = :clubId
              and cm.personProfile.userAccount.email = :email
              and cm.active = true
              and cm.club.clubStatus = :clubStatus
            """)
    Optional<ClubMembershipRole> findRole(@Param("clubId") UUID clubId,
                                          @Param("email") String email,
                                          @Param("clubStatus") ClubStatus clubStatus);

    @Query("""
            select count(cm) > 0
            from ClubMembership cm
            where cm.personProfile.userAccount.id = :userId
              and cm.club.id = :clubId
              and cm.role = :role
              and cm.active = true
            """)
    boolean hasRole(@Param("userId") long userId,
                    @Param("clubId") long clubId,
                    @Param("role") ClubMembershipRole role);

    @Query("""
            select cm
            from ClubMembership cm
            join fetch cm.club c
            join fetch cm.personProfile pp
            where c.externalId = :clubId
              and cm.externalId = :membershipId
              and cm.active = true
              and c.clubStatus = :clubStatus
            """)
    Optional<ClubMembership> findActiveMembership(@Param("clubId") UUID clubId,
                                                  @Param("membershipId") UUID membershipId,
                                                  @Param("clubStatus") ClubStatus clubStatus);

    @Modifying
    @Query("""
            update ClubMembership cm
            set cm.active = false,
                cm.updatedAt = :updatedAt
            where cm.club.id = (
                  select c.id
                  from Club c
                  where c.externalId = :clubId
                    and c.clubStatus = :clubStatus
              )
              and cm.externalId = :membershipId
              and cm.active = true
            """)
    int deactivateMembership(@Param("clubId") UUID clubId,
                             @Param("membershipId") UUID membershipId,
                             @Param("clubStatus") ClubStatus clubStatus,
                             @Param("updatedAt") Instant updatedAt);

    @Modifying
    @Query("""
            update ClubMembership cm
            set cm.active = false,
                cm.updatedAt = :updatedAt
            where cm.club.id = :clubId
              and cm.active = true
            """)
    int deactivateAllByClubId(@Param("clubId") Long clubId,
                              @Param("updatedAt") Instant updatedAt);
}
