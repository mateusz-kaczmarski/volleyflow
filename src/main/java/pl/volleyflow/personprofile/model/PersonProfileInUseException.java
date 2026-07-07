package pl.volleyflow.personprofile.model;

public class PersonProfileInUseException extends RuntimeException {
    public PersonProfileInUseException(String message) {
        super(message);
    }
}
