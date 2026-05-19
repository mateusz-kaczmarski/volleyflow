package pl.volleyflow.user.controller;

import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pl.volleyflow.auth.controller.GlobalErrorResponse;
import pl.volleyflow.user.model.UserAccountAlreadyExists;

@RestControllerAdvice
@Log4j2
public class UserAccountAdviceController {

    @ExceptionHandler(UserAccountAlreadyExists.class)
    public ResponseEntity<GlobalErrorResponse> handleUserAlreadyExists(UserAccountAlreadyExists ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new GlobalErrorResponse("User already exists"));
    }

}
