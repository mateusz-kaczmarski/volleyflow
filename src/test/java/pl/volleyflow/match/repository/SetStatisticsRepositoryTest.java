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
import pl.volleyflow.match.entity.SetStatisticEntity;
import pl.volleyflow.match.model.match.MatchStatus;
import pl.volleyflow.personprofile.model.PersonProfile;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.entity.UserAccountStatus;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class SetStatisticsRepositoryTest {

    @Autowired
    private SetStatisticsRepository statisticsRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void shouldFindStatisticsForSetAndClub() {
        Fixture fixture = saveFixture(MatchStatus.FINISHED);

        assertThat(statisticsRepository.findBySetAndClub(fixture.set(), fixture.club()))
                .extracting(SetStatisticEntity::getExternalId)
                .containsExactly(fixture.statistic().getExternalId());
    }

    @Test
    void shouldIncludeOnlyFinishedMatchesInCareerStatistics() {
        Fixture fixture = saveFixture(MatchStatus.SCHEDULED);

        assertThat(statisticsRepository.findPlayerCareerStatistics(
                fixture.club().getExternalId(), fixture.player().getExternalId(), MatchStatus.FINISHED))
                .isEmpty();
        assertThat(statisticsRepository.findPlayerCareerStatistics(
                fixture.club().getExternalId(), fixture.player().getExternalId(), MatchStatus.SCHEDULED))
                .extracting(SetStatisticEntity::getExternalId)
                .containsExactly(fixture.statistic().getExternalId());
    }

    private Fixture saveFixture(MatchStatus status) {
        Club club = entityManager.persistAndFlush(Club.builder()
                .name("Test club").clubStatus(ClubStatus.ACTIVE).build());
        UserAccount user = entityManager.persistAndFlush(UserAccount.builder()
                .email("user@example.com").passwordHash("hash").status(UserAccountStatus.ACTIVE).build());
        PersonProfile player = entityManager.persistAndFlush(PersonProfile.builder()
                .firstName("Jan").lastName("Kowalski").build());
        Match match = entityManager.persistAndFlush(Match.builder()
                .createdByClub(club).createdBy(user).status(status)
                .scheduledAt(Instant.parse("2026-06-01T12:00:00Z"))
                .seasonNumber(2026)
                .location(MatchAddress.builder().city("Warszawa").zipCode("00-001")
                        .street("Sportowa").buildingNumber("1").build())
                .build());
        SetEntity set = entityManager.persistAndFlush(SetEntity.builder()
                .match(match).setNumber(1).homePoints(25).awayPoints(20).build());
        SetStatisticEntity statistic = entityManager.persistAndFlush(SetStatisticEntity.builder()
                .set(set).club(club).player(player).points(5).build());
        return new Fixture(club, player, set, statistic);
    }

    private record Fixture(Club club, PersonProfile player, SetEntity set, SetStatisticEntity statistic) {
    }
}
