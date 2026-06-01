package pl.volleyflow.personprofile.service;

import pl.volleyflow.personprofile.model.PersonProfileDto;
import pl.volleyflow.personprofile.model.PersonProfileRequest;

public interface PersonProfileService {

    PersonProfileDto createProfile(PersonProfileRequest personProfileRequest);

}
