package pl.volleyflow.match.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import pl.volleyflow.club.model.Club;
import pl.volleyflow.club.model.ClubStatus;
import pl.volleyflow.match.entity.Match;
import pl.volleyflow.match.entity.MatchAddress;
import pl.volleyflow.match.entity.SetEntity;
import pl.volleyflow.match.model.match.MatchStatus;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.entity.UserAccountStatus;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class SetRepositoryTest {

    @Autowired
    private SetRepository setRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldFindSetOnlyInItsMatch() {
        Club club = entityManager.persistAndFlush(Club.builder()
                .name("Test club").clubStatus(ClubStatus.ACTIVE).build());
        UserAccount user = entityManager.persistAndFlush(UserAccount.builder()
                .email("user@example.com").passwordHash("hash").status(UserAccountStatus.ACTIVE).build());
        Match match = saveMatch(club, user);
        Match otherMatch = saveMatch(club, user);
        SetEntity set = entityManager.persistAndFlush(SetEntity.builder()
                .match(match).setNumber(1).homePoints(25).awayPoints(20).build());

        assertThat(setRepository.findByExternalIdAndMatch(set.getExternalId(), match)).contains(set);
        assertThat(setRepository.findByExternalIdAndMatch(set.getExternalId(), otherMatch)).isEmpty();
    }

    private Match saveMatch(Club club, UserAccount user) {
        return entityManager.persistAndFlush(Match.builder()
                .createdByClub(club).createdBy(user).status(MatchStatus.SCHEDULED)
                .scheduledAt(Instant.parse("2026-06-01T12:00:00Z"))
                .seasonNumber(2026)
                .location(MatchAddress.builder().city("Warszawa").zipCode("00-001")
                        .street("Sportowa").buildingNumber("1").build())
                .build());
    }
}
