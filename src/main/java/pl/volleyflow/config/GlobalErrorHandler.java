package pl.volleyflow.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.volleyflow.auth.controller.GlobalErrorResponse;
import pl.volleyflow.auth.exceptions.InvalidCredentialsException;
import pl.volleyflow.club.model.ClubAlreadyExists;
import pl.volleyflow.club.model.ClubNotFoundException;
import pl.volleyflow.clubmembership.exceptions.ClubMembershipAccessDeniedException;
import pl.volleyflow.clubmembership.model.exceptions.ClubMembershipAlreadyExistsException;
import pl.volleyflow.clubmembership.model.exceptions.ClubMembershipNotFoundException;
import pl.volleyflow.personprofile.model.PersonProfileAlreadyExistsException;
import pl.volleyflow.personprofile.model.PersonProfileNotFoundException;
import pl.volleyflow.user.model.UserAccountAlreadyExists;
import pl.volleyflow.user.model.UserNoPermission;
import pl.volleyflow.user.model.UserNotFoundException;

@RestControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<GlobalErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new GlobalErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(UserAccountAlreadyExists.class)
    public ResponseEntity<GlobalErrorResponse> handleUserAlreadyExists(UserAccountAlreadyExists ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new GlobalErrorResponse("User already exists"));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<GlobalErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new GlobalErrorResponse("User not exists"));
    }

    @ExceptionHandler(UserNoPermission.class)
    public ResponseEntity<GlobalErrorResponse> handleUserNoPermission(UserNoPermission ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new GlobalErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ClubAlreadyExists.class)
    public ResponseEntity<GlobalErrorResponse> handleClubAlreadyExists(ClubAlreadyExists ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new GlobalErrorResponse("Club already exists"));
    }

    @ExceptionHandler(ClubNotFoundException.class)
    public ResponseEntity<GlobalErrorResponse> handleClubNotFound(ClubNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new GlobalErrorResponse("Club not found"));
    }

    @ExceptionHandler(ClubMembershipAccessDeniedException.class)
    public ResponseEntity<GlobalErrorResponse> handleClubMembershipAccessDenied(
            ClubMembershipAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new GlobalErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ClubMembershipNotFoundException.class)
    public ResponseEntity<GlobalErrorResponse> handleClubMembershipNotFound(ClubMembershipNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new GlobalErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ClubMembershipAlreadyExistsException.class)
    public ResponseEntity<GlobalErrorResponse> handleClubMembershipAlreadyExists(
            ClubMembershipAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new GlobalErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(PersonProfileAlreadyExistsException.class)
    public ResponseEntity<GlobalErrorResponse> handlePersonProfileAlreadyExists(PersonProfileAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new GlobalErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GlobalErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(new GlobalErrorResponse("Request contains invalid argument"));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body(new GlobalErrorResponse("Validation failed"));
    }

    @ExceptionHandler(PersonProfileNotFoundException.class)
    public ResponseEntity<GlobalErrorResponse> handlePersonProfileNotExistException(PersonProfileNotFoundException ex) {
        return ResponseEntity.notFound().build();
    }

}
