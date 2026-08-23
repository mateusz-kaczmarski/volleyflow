package pl.volleyflow.match.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.volleyflow.match.entity.MatchEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MatchRepository extends JpaRepository<MatchEntity, Long> {

    Optional<MatchEntity> findByExternalId(UUID matchExternalId);

    List<MatchEntity> findByCreatedByClubExternalIdOrderByScheduledAtAsc(UUID clubExternalId);

}
