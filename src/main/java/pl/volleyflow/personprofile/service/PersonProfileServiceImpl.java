package pl.volleyflow.personprofile.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.volleyflow.personprofile.model.PersonProfile;
import pl.volleyflow.personprofile.model.PersonProfileDto;
import pl.volleyflow.personprofile.model.PersonProfileMapper;
import pl.volleyflow.personprofile.model.PersonProfileRequest;
import pl.volleyflow.personprofile.repository.PersonProfileRepository;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.model.UserNotFoundException;
import pl.volleyflow.user.repository.UserAccountRepository;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class PersonProfileServiceImpl implements PersonProfileService {

    private final PersonProfileRepository personProfileRepository;
    private final UserAccountRepository userAccountRepository;

    @Override
    @Transactional
    public PersonProfileDto createProfile(PersonProfileRequest personProfileRequest, String userEmail) {
        UserAccount user = userAccountRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        PersonProfile savedPersonProfile = createProfile(user, personProfileRequest);

        log.info("Created person profile {}", savedPersonProfile.getExternalId());
        return PersonProfileMapper.mapToDto(savedPersonProfile);
    }

    @Override
    @Transactional
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
