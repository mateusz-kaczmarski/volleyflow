package pl.volleyflow.match.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.volleyflow.club.model.Club;
import pl.volleyflow.match.entity.Match;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    Optional<Match> findByExternalId(UUID matchExternalId);

    List<Match> findByCreatedByClubExternalIdOrderByScheduledAtAsc(UUID clubExternalId);

    List<Match> findByCreatedByClubExternalIdAndSeasonNumberOrderByScheduledAtAsc(UUID clubExternalId,
                                                                              int seasonNumber);

    @Query("""
            select distinct m.seasonNumber
            from Match m
            where m.createdByClub = :club
            order by m.seasonNumber desc
            """)
    List<Integer> findDistinctSeasonNumbersByCreatedByClub(@Param("club") Club club);

}
