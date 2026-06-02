package pl.volleyflow.personprofile.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.volleyflow.personprofile.model.PersonProfile;
import pl.volleyflow.user.entity.UserAccount;

import java.util.Optional;

public interface PersonProfileRepository extends JpaRepository<PersonProfile, Long> {

    Optional<PersonProfile> findByUserAccount(UserAccount userAccount);

}

