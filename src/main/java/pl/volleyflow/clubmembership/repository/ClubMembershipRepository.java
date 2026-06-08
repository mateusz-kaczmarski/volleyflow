package pl.volleyflow.clubmembership.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.volleyflow.clubmembership.model.ClubMembership;

public interface ClubMembershipRepository extends JpaRepository<ClubMembership, Long> {
}
