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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class PersonProfileServiceImpl implements PersonProfileService {

    private final PersonProfileRepository personProfileRepository;
    private final UserAccountRepository userAccountRepository;

    @Override
    @Transactional
    public PersonProfileDto createProfile(PersonProfileCreateRequest personProfileCreateRequest, String userEmail) {
        log.info("Starting person profile creation for userEmail={}", userEmail);
        UserAccount user = userAccountRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.warn("Person profile creation failed: userEmail={} not found", userEmail);
                    return new UserNotFoundException("User not found");
                });

        personProfileRepository.findByUserAccount(user).ifPresent(profile -> {
            throw new PersonProfileAlreadyExistsException("Profile already exists");
        });

        PersonProfile savedPersonProfile = createProfile(user, personProfileCreateRequest);

        log.info("Created person profile: profileExternalId={}, userExternalId={}",
                savedPersonProfile.getExternalId(), user.getExternalId());
        return PersonProfileMapper.mapToDto(savedPersonProfile);
    }

    @Override
    @Transactional
    public PersonProfile createProfile(UserAccount userAccount, PersonProfileCreateRequest personProfileCreateRequest) {
        PersonProfile personProfile = PersonProfileMapper.createEntity(personProfileCreateRequest);
        personProfile.setUserAccount(userAccount);
        return personProfileRepository.save(personProfile);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<PersonProfile> findByUserAccount(UserAccount userAccount) {
        return personProfileRepository.findByUserAccount(userAccount);
    }

    @Override
    @Transactional
    public PersonProfileDto updateProfile(PersonProfileUpdateRequest personProfileUpdateRequest, String userEmail) {
        log.info("Starting person profile update for userEmail={}", userEmail);
        UserAccount user = userAccountRepository.findByEmail(userEmail)
                .orElseThrow(() -> {
                    log.warn("Person profile update failed: userEmail={} not found", userEmail);
                    return new UserNotFoundException("User not found");
                });

        PersonProfile personProfile = personProfileRepository.findByUserAccount(user)
                .orElseThrow(() -> new PersonProfileAlreadyExistsException("Profile already exists"));

        PersonProfileMapper.updateEntity(personProfile, personProfileUpdateRequest);
        log.info("Updated person profile: profileExternalId={}, userExternalId={}",
                personProfile.getExternalId(), user.getExternalId());
        return PersonProfileMapper.mapToDto(personProfile);
    }

    @Override
    public PersonProfileDto getProfileByExternalId(UUID profileExternalId) {
        return personProfileRepository.findByExternalId(profileExternalId)
                .map(PersonProfileMapper::mapToDto)
                .orElseThrow(() -> new PersonProfileNotExistException("Profile do not exists"));
    }

}
