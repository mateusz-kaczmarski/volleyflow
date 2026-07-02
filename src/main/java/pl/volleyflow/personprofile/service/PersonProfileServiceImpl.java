package pl.volleyflow.personprofile.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.volleyflow.personprofile.model.*;
import pl.volleyflow.personprofile.repository.PersonProfileRepository;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.model.UserNotFoundException;
import pl.volleyflow.user.repository.UserAccountRepository;

import java.util.Optional;

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
        log.info("Starting person profile creation for userEmail={}", userEmail);
        UserAccount user = userAccountRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.warn("Person profile creation failed: userEmail={} not found", userEmail);
                    return new UserNotFoundException("User not found");
                });

        personProfileRepository.findByUserAccount(user).ifPresent(profile -> {
            throw new PersonProfileAlreadyExists("Profile already exists");
        });

        PersonProfile savedPersonProfile = createProfile(user, personProfileRequest);

        log.info("Created person profile: profileExternalId={}, userExternalId={}",
                savedPersonProfile.getExternalId(), user.getExternalId());
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
    public Optional<PersonProfile> findByUserAccount(UserAccount userAccount) {
        return personProfileRepository.findByUserAccount(userAccount);
    }

}
