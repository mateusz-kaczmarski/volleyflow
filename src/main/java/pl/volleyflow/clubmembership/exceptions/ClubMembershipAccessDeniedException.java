package pl.volleyflow.clubmembership.exceptions;

public class ClubMembershipAccessDeniedException extends RuntimeException {
    public ClubMembershipAccessDeniedException(String message) {
        super(message);
    }
}
