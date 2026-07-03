package pl.volleyflow.personprofile.service;

import pl.volleyflow.personprofile.model.PersonProfile;
import pl.volleyflow.personprofile.model.PersonProfileCreateRequest;
import pl.volleyflow.personprofile.model.PersonProfileDto;
import pl.volleyflow.personprofile.model.PersonProfileUpdateRequest;
import pl.volleyflow.user.entity.UserAccount;

import java.util.Optional;
import java.util.UUID;

public interface PersonProfileService {

    PersonProfileDto createProfile(PersonProfileCreateRequest personProfileCreateRequest, String userEmail);

    PersonProfile createProfile(UserAccount userAccount, PersonProfileCreateRequest personProfileCreateRequest);

    Optional<PersonProfile> findByUserAccount(UserAccount userAccount);

    PersonProfileDto updateProfile(PersonProfileUpdateRequest personProfileUpdateRequest, String userEmail);

    PersonProfileDto getProfileByExternalId(UUID profileExternalId);

    PersonProfileDto getMyProfile(String userEmail);

}
