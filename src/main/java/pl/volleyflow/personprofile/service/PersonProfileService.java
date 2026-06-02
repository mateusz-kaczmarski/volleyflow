package pl.volleyflow.personprofile.service;

import pl.volleyflow.personprofile.model.PersonProfile;
import pl.volleyflow.personprofile.model.PersonProfileDto;
import pl.volleyflow.personprofile.model.PersonProfileRequest;
import pl.volleyflow.user.entity.UserAccount;

import java.util.Optional;

public interface PersonProfileService {

    PersonProfileDto createProfile(PersonProfileRequest personProfileRequest);

    PersonProfile createProfile(UserAccount userAccount, PersonProfileRequest personProfileRequest);

    Optional<PersonProfile> findByUserAccount(UserAccount userAccount);

}
