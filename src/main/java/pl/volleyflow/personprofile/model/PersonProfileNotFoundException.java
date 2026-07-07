package pl.volleyflow.personprofile.model;

public class PersonProfileNotFoundException extends RuntimeException {
    public PersonProfileNotFoundException(String message) {
        super(message);
    }
}
