package pl.volleyflow.club.model;

public class ClubAlreadyExists extends RuntimeException {
    public ClubAlreadyExists(String message) {
        super(message);
    }
}
