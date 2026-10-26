package pl.volleyflow.match.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.volleyflow.club.model.Club;
import pl.volleyflow.match.entity.SetEntity;
import pl.volleyflow.match.entity.SetStatisticEntity;
import pl.volleyflow.match.model.match.MatchStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface SetStatisticsRepository extends JpaRepository<SetStatisticEntity, Long> {

    List<SetStatisticEntity> findBySetAndClub(SetEntity set, Club club);

    @Query("""
            select statistic
            from SetStatisticEntity statistic
            join fetch statistic.set matchSet
            join fetch matchSet.match volleyballMatch
            where statistic.club.externalId = :clubExternalId
              and statistic.player.externalId = :playerExternalId
              and volleyballMatch.status = :status
            order by volleyballMatch.scheduledAt desc, matchSet.setNumber asc
            """)
    List<SetStatisticEntity> findPlayerCareerStatistics(
            @Param("clubExternalId") UUID clubExternalId,
            @Param("playerExternalId") UUID playerExternalId,
            @Param("status") MatchStatus status);
}
