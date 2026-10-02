package pl.volleyflow.match.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.volleyflow.club.model.Club;
import pl.volleyflow.match.entity.SetEntity;
import pl.volleyflow.match.entity.SetStatisticEntity;

import java.util.List;

public interface SetStatisticsRepository extends JpaRepository<SetStatisticEntity, Long> {

    List<SetStatisticEntity> findBySetAndClub(SetEntity set, Club club);

}
