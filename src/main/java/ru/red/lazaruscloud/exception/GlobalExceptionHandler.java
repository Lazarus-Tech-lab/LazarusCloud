package ru.red.lazaruscloud.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

import java.util.Map;
import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> Map.of(
                        "field", error.getField(),
                        "message", Objects.requireNonNull(error.getDefaultMessage())))
                .toList();

        return ResponseEntity.badRequest().body(Map.of("errors", errors));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDbErrors(DataIntegrityViolationException ex) {
        return ResponseEntity.badRequest().body(
                Map.of("error", "username or email already in use")
        );
    }
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> handleAuthError(UsernameNotFoundException ex) {
        return ResponseEntity.badRequest().body(
                Map.of("error", "username or password incorrect")
        );
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<?> handleMultipartErrors(MultipartException ex) {
        return ResponseEntity.badRequest().body(
                Map.of("error", "need file")
        );
    }
}
