package pl.volleyflow.match.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.volleyflow.match.entity.MatchEntity;

@Repository
public interface MatchRepository extends JpaRepository<MatchEntity, Long> {
}
