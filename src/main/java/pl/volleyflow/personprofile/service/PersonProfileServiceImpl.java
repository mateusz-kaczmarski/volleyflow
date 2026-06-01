package pl.volleyflow.personprofile.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pl.volleyflow.personprofile.model.PersonProfile;
import pl.volleyflow.personprofile.model.PersonProfileDto;
import pl.volleyflow.personprofile.model.PersonProfileMapper;
import pl.volleyflow.personprofile.model.PersonProfileRequest;
import pl.volleyflow.personprofile.repository.PersonProfileRepository;

@Service
@RequiredArgsConstructor
@Log4j2
public class PersonProfileServiceImpl implements PersonProfileService {

    private final PersonProfileRepository personProfileRepository;

    @Override
    public PersonProfileDto createProfile(PersonProfileRequest personProfileRequest) {
        PersonProfile personProfile = PersonProfileMapper.mapToEntity(personProfileRequest);
        PersonProfile savedPersonProfile = personProfileRepository.save(personProfile);

        log.info("Created person profile {}", savedPersonProfile.getExternalId());
        return PersonProfileMapper.mapToDto(savedPersonProfile);
    }

}
