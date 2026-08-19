package pl.volleyflow.match.model.exceptions;

public class MatchTeamsMustBeDifferentException extends IllegalArgumentException {
    public MatchTeamsMustBeDifferentException(String message) {
        super(message);
    }
}
