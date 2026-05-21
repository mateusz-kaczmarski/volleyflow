package pl.volleyflow.club.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.volleyflow.auth.controller.GlobalErrorResponse;
import pl.volleyflow.club.model.ClubAlreadyExists;

@RestControllerAdvice
@Log4j2
public class ClubAdviceController {

    @ExceptionHandler(ClubAlreadyExists.class)
    public ResponseEntity<GlobalErrorResponse> handleClubAlreadyExists(ClubAlreadyExists ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new GlobalErrorResponse("Club already exists"));
    }
}
