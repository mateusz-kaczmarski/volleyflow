package pl.volleyflow.personprofile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.volleyflow.personprofile.model.PersonProfile;

public interface PersonProfileRepository extends JpaRepository<PersonProfile, Long> {
}
