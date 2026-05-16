package pl.volleyflow.user.model;

public class UserAccountAlreadyExists extends RuntimeException {
    public UserAccountAlreadyExists(String message) {
        super(message);
    }
}
