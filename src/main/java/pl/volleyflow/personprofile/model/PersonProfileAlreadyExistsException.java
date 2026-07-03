package pl.volleyflow.personprofile.model;

public class PersonProfileAlreadyExistsException extends RuntimeException {
    public PersonProfileAlreadyExistsException(String message) {
        super(message);
    }
}
