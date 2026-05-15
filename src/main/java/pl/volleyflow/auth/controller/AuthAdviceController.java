package pl.volleyflow.auth.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.volleyflow.auth.exceptions.InvalidCredentialsException;

@RestControllerAdvice
@Log4j2
public class AuthAdviceController {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<GlobalErrorResponse> handleInvalidCredentialsException(InvalidCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new GlobalErrorResponse(ex.getMessage()));
    }

}
