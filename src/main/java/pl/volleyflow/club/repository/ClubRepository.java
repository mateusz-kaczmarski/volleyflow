package pl.volleyflow.club.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.volleyflow.club.model.Club;

public interface ClubRepository extends JpaRepository<Club, Long> {

    boolean existsByName(String name);

}
