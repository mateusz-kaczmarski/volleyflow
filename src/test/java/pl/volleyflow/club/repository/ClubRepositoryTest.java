package pl.volleyflow.club.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import pl.volleyflow.club.model.Club;
import pl.volleyflow.club.model.ClubStatus;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ClubRepositoryTest {

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void existsByNameAndClubStatus() {
        Club club = createClub("Club name", ClubStatus.ACTIVE);
        entityManager.persistAndFlush(club);

        boolean exists = clubRepository.existsByNameAndClubStatus("Club name", ClubStatus.ACTIVE);

        assertThat(exists).isTrue();
    }

    @Test
    void existsByNameAndClubStatusReturnFalseWhenStatusIsDifferent() {
        Club club = createClub("Club name", ClubStatus.DELETED);
        entityManager.persistAndFlush(club);

        boolean exists = clubRepository.existsByNameAndClubStatus("Club name", ClubStatus.ACTIVE);

        assertThat(exists).isFalse();
    }

    @Test
    void findByNameAndClubStatus() {
        Club club = createClub("Club name", ClubStatus.ACTIVE);
        Club savedClub = entityManager.persistAndFlush(club);

        Optional<Club> foundClub = clubRepository.findByNameAndClubStatus("Club name", ClubStatus.ACTIVE);

        assertThat(foundClub)
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .isEqualTo(savedClub);
    }

    @Test
    void findByNameAndClubStatusReturnEmptyWhenClubDoesNotExist() {
        Optional<Club> foundClub = clubRepository.findByNameAndClubStatus("Club name", ClubStatus.ACTIVE);

        assertThat(foundClub).isEmpty();
    }

    @Test
    void findByExternalIdAndClubStatus() {
        Club club = createClub("Club name", ClubStatus.ACTIVE);
        Club savedClub = entityManager.persistAndFlush(club);

        Optional<Club> foundClub = clubRepository.findByExternalIdAndClubStatus(savedClub.getExternalId(), ClubStatus.ACTIVE);

        assertThat(foundClub)
                .isPresent()
                .get()
                .usingRecursiveComparison()
                .isEqualTo(savedClub);
    }

    @Test
    void findByExternalIdAndClubStatusReturnEmptyWhenStatusIsDifferent() {
        Club club = createClub("Club name", ClubStatus.DELETED);
        Club savedClub = entityManager.persistAndFlush(club);

        Optional<Club> foundClub = clubRepository.findByExternalIdAndClubStatus(savedClub.getExternalId(), ClubStatus.ACTIVE);

        assertThat(foundClub).isEmpty();
    }

    @Test
    void updateStatus() {
        Club club = createClub("Club name", ClubStatus.ACTIVE);
        Club savedClub = entityManager.persistAndFlush(club);

        int updatedRows = clubRepository.updateStatus(savedClub.getId(), ClubStatus.ACTIVE, ClubStatus.DELETED, Instant.now());
        entityManager.clear();

        Club updatedClub = entityManager.find(Club.class, savedClub.getId());
        assertThat(updatedRows).isEqualTo(1);
        assertThat(updatedClub.getClubStatus()).isEqualTo(ClubStatus.DELETED);
    }

    @Test
    void updateStatusReturnZeroWhenCurrentStatusIsDifferent() {
        Club club = createClub("Club name", ClubStatus.DELETED);
        Club savedClub = entityManager.persistAndFlush(club);

        int updatedRows = clubRepository.updateStatus(savedClub.getId(), ClubStatus.ACTIVE, ClubStatus.DELETED, Instant.now());
        entityManager.clear();

        Club updatedClub = entityManager.find(Club.class, savedClub.getId());
        assertThat(updatedRows).isZero();
        assertThat(updatedClub.getClubStatus()).isEqualTo(ClubStatus.DELETED);
    }

    private Club createClub(String name, ClubStatus clubStatus) {
        return Club.builder()
                .name(name)
                .avatar("avatar.jpg")
                .description("description")
                .clubStatus(clubStatus)
                .build();
    }

}
