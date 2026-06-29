package pl.volleyflow.clubmembership.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.volleyflow.auth.controller.GlobalErrorResponse;
import pl.volleyflow.clubmembership.exceptions.ClubMembershipAccessDeniedException;
import pl.volleyflow.clubmembership.model.exceptions.ClubMembershipAlreadyExistsException;
import pl.volleyflow.clubmembership.model.exceptions.ClubMembershipNotFoundException;

@RestControllerAdvice
public class ClubMembershipAdviceController {

    @ExceptionHandler(ClubMembershipAccessDeniedException.class)
    public ResponseEntity<GlobalErrorResponse> handleClubMembershipAccessDenied(
            ClubMembershipAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new GlobalErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ClubMembershipNotFoundException.class)
    public ResponseEntity<GlobalErrorResponse> handleClubMembershipNotFoundException(
            ClubMembershipNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new GlobalErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(ClubMembershipAlreadyExistsException.class)
    public ResponseEntity<GlobalErrorResponse> handleClubMembershipAlreadyExistsException(
            ClubMembershipAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new GlobalErrorResponse(ex.getMessage()));
    }

}
