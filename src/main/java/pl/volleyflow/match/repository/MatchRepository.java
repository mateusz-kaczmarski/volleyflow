package pl.volleyflow.match.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.volleyflow.match.entity.Match;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {

    Optional<Match> findByExternalId(UUID matchExternalId);

    List<Match> findByCreatedByClubExternalIdOrderByScheduledAtAsc(UUID clubExternalId);

}
