package pl.volleyflow.club.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.volleyflow.auth.controller.GlobalErrorResponse;
import pl.volleyflow.club.model.ClubAlreadyExists;
import pl.volleyflow.club.model.ClubNotFoundException;
import pl.volleyflow.user.model.UserNoPermission;

@RestControllerAdvice
public class ClubAdviceController {

    @ExceptionHandler(ClubAlreadyExists.class)
    public ResponseEntity<GlobalErrorResponse> handleClubAlreadyExists(ClubAlreadyExists ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new GlobalErrorResponse("Club already exists"));
    }

    @ExceptionHandler(ClubNotFoundException.class)
    public ResponseEntity<GlobalErrorResponse> handleClubNotFound(ClubNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new GlobalErrorResponse("Club not found"));
    }

    @ExceptionHandler(UserNoPermission.class)
    public ResponseEntity<GlobalErrorResponse> handleUserNoPermission(UserNoPermission ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new GlobalErrorResponse(ex.getMessage()));
    }

}
