package pl.volleyflow.personprofile.model;

public class PersonProfileAlreadyExists extends RuntimeException {
    public PersonProfileAlreadyExists(String message) {
        super(message);
    }
}
