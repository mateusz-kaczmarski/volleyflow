package pl.volleyflow.personprofile.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import pl.volleyflow.personprofile.model.PersonProfile;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.entity.UserAccountStatus;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class PersonProfileRepositoryTest {

    @Autowired
    private PersonProfileRepository personProfileRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldFindProfileByUserAccount() {
        UserAccount user = saveUser();
        PersonProfile profile = saveProfile(user);

        assertThat(personProfileRepository.findByUserAccount(user))
                .isPresent()
                .get()
                .extracting(PersonProfile::getExternalId)
                .isEqualTo(profile.getExternalId());
    }

    @Test
    void shouldFindProfileByExternalId() {
        PersonProfile profile = saveProfile(saveUser());

        assertThat(personProfileRepository.findByExternalId(profile.getExternalId()))
                .isPresent()
                .get()
                .extracting(PersonProfile::getId)
                .isEqualTo(profile.getId());
    }

    @Test
    void shouldReturnEmptyForUnknownExternalId() {
        assertThat(personProfileRepository.findByExternalId(UUID.randomUUID())).isEmpty();
    }

    private UserAccount saveUser() {
        return entityManager.persistAndFlush(UserAccount.builder()
                .email("user@example.com")
                .passwordHash("hash")
                .status(UserAccountStatus.ACTIVE)
                .emailVerified(true)
                .build());
    }

    private PersonProfile saveProfile(UserAccount user) {
        return entityManager.persistAndFlush(PersonProfile.builder()
                .userAccount(user)
                .firstName("Jan")
                .lastName("Kowalski")
                .build());
    }
}
