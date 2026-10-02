package pl.volleyflow.match.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.volleyflow.match.entity.Match;
import pl.volleyflow.match.entity.SetEntity;

import java.util.Optional;
import java.util.UUID;

public interface SetRepository extends JpaRepository<SetEntity, Long> {

    Optional<SetEntity> findByExternalIdAndMatch(UUID setExternalId, Match match);

    Optional<SetEntity> findByExternalId(UUID setExternalId);



}
