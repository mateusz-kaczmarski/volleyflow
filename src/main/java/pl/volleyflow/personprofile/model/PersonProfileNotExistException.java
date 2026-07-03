package pl.volleyflow.personprofile.model;

public class PersonProfileNotExistException extends RuntimeException {
    public PersonProfileNotExistException(String message) {
        super(message);
    }
}
