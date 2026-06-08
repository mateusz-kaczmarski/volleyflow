package pl.volleyflow.clubmembership.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import pl.volleyflow.club.model.Club;
import pl.volleyflow.club.model.ClubNotFoundException;
import pl.volleyflow.club.repository.ClubRepository;
import pl.volleyflow.clubmembership.model.ClubMembership;
import pl.volleyflow.clubmembership.model.ClubMembershipDto;
import pl.volleyflow.clubmembership.model.ClubMembershipMapper;
import pl.volleyflow.clubmembership.model.ClubMembershipRequest;
import pl.volleyflow.clubmembership.repository.ClubMembershipRepository;
import pl.volleyflow.personprofile.model.PersonProfile;
import pl.volleyflow.personprofile.repository.PersonProfileRepository;

@Service
@RequiredArgsConstructor
@Log4j2
public class ClubMembershipServiceImpl implements ClubMembershipService {

    private final ClubRepository clubRepository;
    private final PersonProfileRepository personProfileRepository;
    private final ClubMembershipRepository clubMembershipRepository;

    @Override
    public ClubMembershipDto createMembership(ClubMembershipRequest request) {
        log.info("Start create club membership {}", request);

        Club club = clubRepository.findByExternalId(request.clubExternalId())
                .orElseThrow(() -> new ClubNotFoundException("Club not found"));

        PersonProfile personProfile = ClubMembershipMapper.mapToPersonProfile(request);
        PersonProfile savedPersonProfile = personProfileRepository.save(personProfile);

        ClubMembership membership = ClubMembershipMapper.mapToEntity(request);
        membership.setClub(club);
        membership.setPersonProfile(savedPersonProfile);

        ClubMembership savedMembership = clubMembershipRepository.save(membership);
        log.info("Saved club membership {}", savedMembership.getExternalId());

        return ClubMembershipMapper.mapToDto(savedMembership);
    }
}
