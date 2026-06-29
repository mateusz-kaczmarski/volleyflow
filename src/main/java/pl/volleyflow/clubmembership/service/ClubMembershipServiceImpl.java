package pl.volleyflow.clubmembership.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.volleyflow.club.model.Club;
import pl.volleyflow.club.model.ClubNotFoundException;
import pl.volleyflow.club.repository.ClubRepository;
import pl.volleyflow.clubmembership.exceptions.ClubMembershipAccessDeniedException;
import pl.volleyflow.clubmembership.model.*;
import pl.volleyflow.clubmembership.model.exceptions.ClubMembershipAlreadyExistsException;
import pl.volleyflow.clubmembership.model.exceptions.ClubMembershipNotFoundException;
import pl.volleyflow.clubmembership.repository.ClubMembershipRepository;
import pl.volleyflow.personprofile.model.PersonProfile;
import pl.volleyflow.personprofile.repository.PersonProfileRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class ClubMembershipServiceImpl implements ClubMembershipService {

    private final ClubRepository clubRepository;
    private final PersonProfileRepository personProfileRepository;
    private final ClubMembershipRepository clubMembershipRepository;

    @Override
    @Transactional
    public ClubMembershipDto createMembership(ClubMembershipCreateRequest request, String userEmail) {
        log.info("Start create club membership {}", request);

        Club club = clubRepository.findActiveByExternalId(request.clubExternalId())
                .orElseThrow(() -> new ClubNotFoundException("Club not found"));

        validateCreateMembership(request);

        PersonProfile personProfile = ClubMembershipMapper.mapToPersonProfile(request);
        PersonProfile savedPersonProfile = personProfileRepository.save(personProfile);

        ClubMembership membership = ClubMembershipMapper.mapToEntity(request);
        membership.setClub(club);
        membership.setPersonProfile(savedPersonProfile);

        ClubMembership savedMembership = clubMembershipRepository.save(membership);
        log.info("Saved club membership {}", savedMembership.getExternalId());

        return ClubMembershipMapper.mapToDto(savedMembership);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClubMembershipDto> getPlayersByClub(UUID clubExternalId,
                                                    String userEmail,
                                                    Boolean active) {
        if (!clubMembershipRepository.isClubMember(clubExternalId, userEmail)) {
            throw new ClubMembershipAccessDeniedException("You do not have access to this club");
        }

        return clubMembershipRepository.findMembershipsByClubRoleAndActiveFilter(
                        clubExternalId,
                        ClubMembershipRole.PLAYER.name(),
                        active
                )
                .stream()
                .map(ClubMembershipMapper::mapToDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClubMembershipDto getClubMembershipDetails(UUID clubExternalId,
                                                      UUID membershipExternalId,
                                                      String userEmail) {
        if (!clubMembershipRepository.isClubMember(clubExternalId, userEmail)) {
            throw new ClubMembershipAccessDeniedException("You do not have access to this club");
        }

        ClubMembership membership = clubMembershipRepository
                .findMembershipByClubExternalIdAndMembershipExternalId(clubExternalId, membershipExternalId)
                .orElseThrow(() -> new ClubMembershipNotFoundException("Club membership not found"));

        return ClubMembershipMapper.mapToDto(membership);
    }

    @Override
    @Transactional
    public ClubMembershipDto updateMembership(UUID clubExternalId,
                                              UUID membershipExternalId,
                                              ClubMembershipUpdateRequest request,
                                              String userEmail) {
        if (!clubMembershipRepository.canManageClubMemberships(clubExternalId, userEmail)) {
            throw new ClubMembershipAccessDeniedException("You cannot manage memberships in this club");
        }

        ClubMembership membership = clubMembershipRepository
                .findMembershipByClubExternalIdAndMembershipExternalId(clubExternalId, membershipExternalId)
                .orElseThrow(() -> new ClubMembershipNotFoundException("Club membership not found"));

        validateUpdateMembership(clubExternalId, membershipExternalId, request);

        ClubMembershipMapper.updateEntity(membership, request);
        return ClubMembershipMapper.mapToDto(membership);
    }

    @Override
    @Transactional
    public void deleteMembership(UUID clubExternalId, UUID membershipExternalId, String userEmail) {
        if (!clubMembershipRepository.canManageClubMemberships(clubExternalId, userEmail)) {
            throw new ClubMembershipAccessDeniedException("You cannot manage memberships in this club");
        }

        int updatedRows = clubMembershipRepository.deactivateByClubExternalIdAndMembershipExternalId(
                clubExternalId,
                membershipExternalId
        );
        if (updatedRows == 0) {
            throw new ClubMembershipNotFoundException("Club membership not found");
        }
    }

    private void validateCreateMembership(ClubMembershipCreateRequest request) {
        if (clubMembershipRepository.existsActivePlayerInClub(
                request.clubExternalId(),
                request.firstName(),
                request.lastName())) {
            throw new ClubMembershipAlreadyExistsException("Player already exists in this club");
        }

        if (request.shirtNumber() != null && clubMembershipRepository.existsActiveShirtNumberInClub(
                request.clubExternalId(),
                request.shirtNumber())) {
            throw new ClubMembershipAlreadyExistsException("Shirt number already exists in this club");
        }
    }

    private void validateUpdateMembership(
            UUID clubExternalId,
            UUID membershipExternalId,
            ClubMembershipUpdateRequest request) {
        if (clubMembershipRepository.existsActivePlayerInClubExcludingMembership(
                clubExternalId,
                request.firstName(),
                request.lastName(),
                membershipExternalId)) {
            throw new ClubMembershipAlreadyExistsException("Player already exists in this club");
        }

        if (request.shirtNumber() != null
                && clubMembershipRepository.existsActiveShirtNumberInClubExcludingMembership(
                clubExternalId,
                request.shirtNumber(),
                membershipExternalId)) {
            throw new ClubMembershipAlreadyExistsException("Shirt number already exists in this club");
        }
    }

}
