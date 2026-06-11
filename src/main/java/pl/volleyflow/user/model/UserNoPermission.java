package pl.volleyflow.user.model;

public class UserNoPermission extends RuntimeException {
    public UserNoPermission(String message) {
        super(message);
    }
}
