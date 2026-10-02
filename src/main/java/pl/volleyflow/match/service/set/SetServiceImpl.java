package pl.volleyflow.match.service.set;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.volleyflow.club.model.Club;
import pl.volleyflow.club.model.ClubNotFoundException;
import pl.volleyflow.club.model.ClubStatus;
import pl.volleyflow.club.repository.ClubRepository;
import pl.volleyflow.clubmembership.exceptions.ClubMembershipAccessDeniedException;
import pl.volleyflow.clubmembership.model.ClubMembershipRole;
import pl.volleyflow.clubmembership.repository.ClubMembershipRepository;
import pl.volleyflow.match.entity.Match;
import pl.volleyflow.match.entity.SetEntity;
import pl.volleyflow.match.model.Set.SetCreateRequest;
import pl.volleyflow.match.model.Set.SetDto;
import pl.volleyflow.match.model.Set.SetMapper;
import pl.volleyflow.match.model.Set.SetUpdateRequest;
import pl.volleyflow.match.model.exceptions.MatchNotFoundException;
import pl.volleyflow.match.model.exceptions.SetNotFoundException;
import pl.volleyflow.match.repository.MatchRepository;
import pl.volleyflow.match.repository.SetRepository;
import pl.volleyflow.user.entity.UserAccount;
import pl.volleyflow.user.model.UserNotFoundException;
import pl.volleyflow.user.service.UserAccountService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class SetServiceImpl implements SetService {

    private final UserAccountService userAccountService;
    private final ClubRepository clubRepository;
    private final MatchRepository matchRepository;
    private final ClubMembershipRepository clubMembershipRepository;
    private final SetRepository setRepository;

    @Transactional
    @Override
    public SetDto createSet(UUID matchExternalId,
                            UUID clubExternalId,
                            SetCreateRequest setCreateRequest,
                            String email) {
        UserAccount userAccount = getUserOrThrow(email);
        Club club = getClubByExternalId(clubExternalId);
        Match match = getMatchOrThrow(matchExternalId);

        requireStaffRole(userAccount, club);
        requireClubParticipatesInMatch(club, match);

        int setNumber = match.getSets().stream()
                .mapToInt(SetEntity::getSetNumber)
                .max().orElse(0) + 1;

        SetEntity set = SetMapper.mapToEntity(setCreateRequest, match, setNumber);
        setRepository.save(set);

        return SetMapper.mapToDto(set);
    }

    @Transactional
    @Override
    public SetDto updateSet(UUID matchExternalId,
                            UUID clubExternalId,
                            UUID setExternalId,
                            SetUpdateRequest setUpdateRequest,
                            String email) {
        UserAccount userAccount = getUserOrThrow(email);
        Match match = getMatchOrThrow(matchExternalId);
        SetEntity set = findSetOrThrow(setExternalId, match);
        Club club = getClubByExternalId(clubExternalId);

        requireStaffRole(userAccount, club);

        SetMapper.updateEntity(set, setUpdateRequest);

        return SetMapper.mapToDto(set);
    }

    @Transactional
    @Override
    public SetDto updateSetVideo(UUID matchExternalId,
                                 UUID clubExternalId,
                                 UUID setExternalId,
                                 String videoUrl,
                                 String email) {
        UserAccount userAccount = getUserOrThrow(email);
        Match match = getMatchOrThrow(matchExternalId);
        SetEntity set = findSetOrThrow(setExternalId, match);
        Club club = getClubByExternalId(clubExternalId);

        requireStaffRole(userAccount, club);
        requireClubParticipatesInMatch(club, match);
        set.setVideoUrl(videoUrl == null || videoUrl.isBlank() ? null : videoUrl.trim());

        return SetMapper.mapToDto(set);
    }

    @Transactional
    @Override
    public void deleteSet(UUID matchExternalId, UUID clubExternalId, UUID setExternalId, String email) {
        UserAccount userAccount = getUserOrThrow(email);
        Match match = getMatchOrThrow(matchExternalId);
        SetEntity set = findSetOrThrow(setExternalId, match);
        Club club = getClubByExternalId(clubExternalId);

        requireStaffRole(userAccount, club);

        setRepository.delete(set);
    }

    private void requireClubParticipatesInMatch(Club club, Match match) {
        boolean participates = match.getTeams().stream()
                .anyMatch(team -> team.getClub().getId().equals(club.getId()));

        if (!participates) {
            throw new ClubMembershipAccessDeniedException(
                    "Club does not participate in this match");
        }
    }

    private SetEntity findSetOrThrow(UUID setExternalId, Match match) {
        return setRepository.findByExternalIdAndMatch(setExternalId, match)
                .orElseThrow(() -> new SetNotFoundException("Set not found"));
    }

    private Match getMatchOrThrow(UUID matchExternalId) {
        return matchRepository.findByExternalId(matchExternalId)
                .orElseThrow(() -> new MatchNotFoundException("Match nor found"));
    }

    private UserAccount getUserOrThrow(String email) {
        return userAccountService.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    private void requireStaffRole(UserAccount userAccount, Club club) {
        boolean canManageClub = clubMembershipRepository.hasAnyRole(
                userAccount.getId(),
                club.getId(),
                ClubMembershipRole.getStaffRoles()
        );

        if (!canManageClub) {
            log.warn("User {} cannot {}. Club id: {}",
                    userAccount.getId(), "create / update match for selected club", club.getId());
            throw new ClubMembershipAccessDeniedException("You cannot " + "create / update match for selected club");
        }
    }

    private Club getClubByExternalId(UUID request) {
        return clubRepository.findByExternalIdAndClubStatus(request, ClubStatus.ACTIVE)
                .orElseThrow(() -> new ClubNotFoundException("Club not found"));
    }

}
