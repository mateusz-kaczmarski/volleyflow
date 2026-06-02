package pl.volleyflow.personprofile.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pl.volleyflow.personprofile.model.PersonProfile;
import pl.volleyflow.personprofile.model.PersonProfileDto;
import pl.volleyflow.personprofile.model.PersonProfileMapper;
import pl.volleyflow.personprofile.model.PersonProfileRequest;
import pl.volleyflow.personprofile.repository.PersonProfileRepository;
import pl.volleyflow.user.entity.UserAccount;

@Service
@RequiredArgsConstructor
@Log4j2
public class PersonProfileServiceImpl implements PersonProfileService {

    private final PersonProfileRepository personProfileRepository;

    @Override
    public PersonProfileDto createProfile(PersonProfileRequest personProfileRequest) {
        PersonProfile savedPersonProfile = createProfile(null, personProfileRequest);

        log.info("Created person profile {}", savedPersonProfile.getExternalId());
        return PersonProfileMapper.mapToDto(savedPersonProfile);
    }

    @Override
    public PersonProfile createProfile(UserAccount userAccount, PersonProfileRequest personProfileRequest) {
        PersonProfile personProfile = PersonProfileMapper.mapToEntity(personProfileRequest);
        personProfile.setUserAccount(userAccount);
        return personProfileRepository.save(personProfile);
    }

    @Override
    public java.util.Optional<PersonProfile> findByUserAccount(UserAccount userAccount) {
        return personProfileRepository.findByUserAccount(userAccount);
    }

}
