package pl.volleyflow.user.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.entity.UserAccountStatus;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserAccountRepositoryTest {

    private static final String EMAIL = "email@example.pl";
    private static final String OTHER_EMAIL = "other@example.pl";
    private static final String PHONE = "123456789";
    private static final String OTHER_PHONE = "987654321";

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldReturnTrueWhenEmailExists() {
        entityManager.persistAndFlush(createUserAccount(EMAIL, PHONE));

        boolean exists = userAccountRepository.existsByEmail(EMAIL);

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenEmailDoesNotExist() {
        entityManager.persistAndFlush(createUserAccount(EMAIL, PHONE));

        boolean exists = userAccountRepository.existsByEmail(OTHER_EMAIL);

        assertThat(exists).isFalse();
    }

    @Test
    void shouldReturnTrueWhenEmailExistsForDifferentUser() {
        UserAccount userAccount = entityManager.persistAndFlush(createUserAccount(EMAIL, PHONE));

        boolean exists = userAccountRepository.existsByEmailAndIdNot(EMAIL, userAccount.getId() + 1);

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenEmailBelongsToSameUser() {
        UserAccount userAccount = entityManager.persistAndFlush(createUserAccount(EMAIL, PHONE));

        boolean exists = userAccountRepository.existsByEmailAndIdNot(EMAIL, userAccount.getId());

        assertThat(exists).isFalse();
    }

    @Test
    void shouldReturnTrueWhenPhoneExists() {
        entityManager.persistAndFlush(createUserAccount(EMAIL, PHONE));

        boolean exists = userAccountRepository.existsByPhone(PHONE);

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenPhoneDoesNotExist() {
        entityManager.persistAndFlush(createUserAccount(EMAIL, PHONE));

        boolean exists = userAccountRepository.existsByPhone(OTHER_PHONE);

        assertThat(exists).isFalse();
    }

    @Test
    void shouldReturnTrueWhenPhoneExistsForDifferentUser() {
        UserAccount userAccount = entityManager.persistAndFlush(createUserAccount(EMAIL, PHONE));

        boolean exists = userAccountRepository.existsByPhoneAndIdNot(PHONE, userAccount.getId() + 1);

        assertThat(exists).isTrue();
    }

    @Test
    void shouldReturnFalseWhenPhoneBelongsToSameUser() {
        UserAccount userAccount = entityManager.persistAndFlush(createUserAccount(EMAIL, PHONE));

        boolean exists = userAccountRepository.existsByPhoneAndIdNot(PHONE, userAccount.getId());

        assertThat(exists).isFalse();
    }

    @Test
    void shouldFindByEmail() {
        UserAccount savedUserAccount = entityManager.persistAndFlush(createUserAccount(EMAIL, PHONE));

        Optional<UserAccount> result = userAccountRepository.findByEmail(EMAIL);

        assertThat(result)
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .isEqualTo(savedUserAccount);
    }

    @Test
    void shouldReturnEmptyWhenUserDoesNotExistByEmail() {
        Optional<UserAccount> result = userAccountRepository.findByEmail(EMAIL);

        assertThat(result).isEmpty();
    }

    private UserAccount createUserAccount(String email, String phone) {
        return UserAccount.builder()
                .email(email)
                .passwordHash("password-hash")
                .phone(phone)
                .status(UserAccountStatus.ACTIVE)
                .emailVerified(true)
                .build();
    }

}
