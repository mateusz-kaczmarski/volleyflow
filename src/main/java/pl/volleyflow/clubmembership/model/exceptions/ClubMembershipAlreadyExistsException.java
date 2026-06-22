package pl.volleyflow.clubmembership.model.exceptions;

public class ClubMembershipAlreadyExistsException extends RuntimeException {
    public ClubMembershipAlreadyExistsException(String message) {
        super(message);
    }
}
