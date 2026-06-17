package pl.volleyflow.clubmembership.model.exceptions;

public class ClubMembershipNotFoundException extends RuntimeException {
    public ClubMembershipNotFoundException(String message) {
        super(message);
    }
}
