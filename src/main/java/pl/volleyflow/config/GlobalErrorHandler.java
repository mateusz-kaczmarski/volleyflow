package pl.volleyflow.config;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.volleyflow.auth.controller.GlobalErrorResponse;
import pl.volleyflow.auth.controller.GlobalErrorResponse.FieldErrorResponse;
import pl.volleyflow.auth.exceptions.InvalidCredentialsException;
import pl.volleyflow.club.model.ClubAlreadyExists;
import pl.volleyflow.club.model.ClubNotFoundException;
import pl.volleyflow.clubmembership.exceptions.ClubMembershipAccessDeniedException;
import pl.volleyflow.clubmembership.model.exceptions.ClubMembershipAlreadyExistsException;
import pl.volleyflow.clubmembership.model.exceptions.ClubMembershipNotFoundException;
import pl.volleyflow.personprofile.model.PersonProfileAlreadyExistsException;
import pl.volleyflow.personprofile.model.PersonProfileInUseException;
import pl.volleyflow.personprofile.model.PersonProfileNotFoundException;
import pl.volleyflow.user.model.UserAccountAlreadyExists;
import pl.volleyflow.user.model.UserNoPermission;
import pl.volleyflow.user.model.UserNotFoundException;

import java.util.List;

@RestControllerAdvice
public class GlobalErrorHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<GlobalErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        return error(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(UserAccountAlreadyExists.class)
    public ResponseEntity<GlobalErrorResponse> handleUserAlreadyExists(UserAccountAlreadyExists ex) {
        return error(HttpStatus.CONFLICT, "User already exists");
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<GlobalErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "User not exists");
    }

    @ExceptionHandler(UserNoPermission.class)
    public ResponseEntity<GlobalErrorResponse> handleUserNoPermission(UserNoPermission ex) {
        return error(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(ClubAlreadyExists.class)
    public ResponseEntity<GlobalErrorResponse> handleClubAlreadyExists(ClubAlreadyExists ex) {
        return error(HttpStatus.CONFLICT, "Club already exists");
    }

    @ExceptionHandler(ClubNotFoundException.class)
    public ResponseEntity<GlobalErrorResponse> handleClubNotFound(ClubNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, "Club not found");
    }

    @ExceptionHandler(ClubMembershipAccessDeniedException.class)
    public ResponseEntity<GlobalErrorResponse> handleClubMembershipAccessDenied(
            ClubMembershipAccessDeniedException ex) {
        return error(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(ClubMembershipNotFoundException.class)
    public ResponseEntity<GlobalErrorResponse> handleClubMembershipNotFound(ClubMembershipNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ClubMembershipAlreadyExistsException.class)
    public ResponseEntity<GlobalErrorResponse> handleClubMembershipAlreadyExists(
            ClubMembershipAlreadyExistsException ex) {
        return error(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(PersonProfileAlreadyExistsException.class)
    public ResponseEntity<GlobalErrorResponse> handlePersonProfileAlreadyExists(PersonProfileAlreadyExistsException ex) {
        return error(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(PersonProfileInUseException.class)
    public ResponseEntity<GlobalErrorResponse> handlePersonProfileInUse(PersonProfileInUseException ex) {
        return error(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GlobalErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        return error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GlobalErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<FieldErrorResponse> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> new FieldErrorResponse(
                        fieldError.getField(),
                        sanitizeRejectedValue(fieldError.getField(), fieldError.getRejectedValue()),
                        fieldError.getDefaultMessage()
                ))
                .toList();

        return ResponseEntity.badRequest().body(GlobalErrorResponse.validationError(fieldErrors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<GlobalErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<FieldErrorResponse> fieldErrors = ex.getConstraintViolations().stream()
                .map(violation -> new FieldErrorResponse(
                        violation.getPropertyPath().toString(),
                        sanitizeRejectedValue(violation.getPropertyPath().toString(), violation.getInvalidValue()),
                        violation.getMessage()
                ))
                .toList();

        return ResponseEntity.badRequest().body(GlobalErrorResponse.validationError(fieldErrors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<GlobalErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        return error(HttpStatus.BAD_REQUEST, "Request body is missing or malformed");
    }

    @ExceptionHandler(PersonProfileNotFoundException.class)
    public ResponseEntity<GlobalErrorResponse> handlePersonProfileNotExistException(PersonProfileNotFoundException ex) {
        return error(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    private ResponseEntity<GlobalErrorResponse> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(GlobalErrorResponse.of(status, message));
    }

    private Object sanitizeRejectedValue(String field, Object rejectedValue) {
        return field.toLowerCase().contains("password") ? null : rejectedValue;
    }

}
