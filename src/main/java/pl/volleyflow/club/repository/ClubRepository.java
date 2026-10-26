package pl.volleyflow.club.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import pl.volleyflow.club.model.Club;
import pl.volleyflow.club.model.ClubStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClubRepository extends JpaRepository<Club, Long> {

    boolean existsByNameAndClubStatus(String name, ClubStatus status);

    Optional<Club> findByNameAndClubStatus(String name, ClubStatus status);

    Optional<Club> findByExternalIdAndClubStatus(UUID externalId, ClubStatus status);

    @Modifying
    @Query("""
            update Club c
            set c.clubStatus = :newStatus,
                c.updatedAt = :updatedAt
            where c.id = :clubId
              and c.clubStatus = :currentStatus
            """)
    int updateStatus(@Param("clubId") long clubId,
                     @Param("currentStatus") ClubStatus currentStatus,
                     @Param("newStatus") ClubStatus newStatus,
                     @Param("updatedAt") Instant updatedAt);

    @Query("""
            select c
            from Club c
            where lower(c.name) like lower(concat('%', :query, '%'))
              and c.clubStatus = :status
            order by c.name
            """)
    List<Club> findActiveByNameContaining(@Param("query") String query,
                                          @Param("status") ClubStatus status);

}
