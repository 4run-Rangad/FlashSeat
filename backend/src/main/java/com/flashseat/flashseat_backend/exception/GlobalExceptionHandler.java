package com.flashseat.flashseat_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SeatAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleSeatAlreadyExists (
                    SeatAlreadyExistsException exception
            ) {
        return Map.of(
                "message", exception.getMessage()
        );
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleUserNotFound(
            UserNotFoundException exception
    ) {
        return Map.of(
                "message", exception.getMessage()
        );
    }

    @ExceptionHandler(EventNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleEventNotFound(
            EventNotFoundException exception
    ) {
        return Map.of("message", exception.getMessage());
    }

    @ExceptionHandler(SeatNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleSeatNotFound(
            SeatNotFoundException exception
    ) {
        return Map.of("message", exception.getMessage());
    }

    @ExceptionHandler(SeatNotAvailableException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleSeatNotAvailable(
            SeatNotAvailableException exception
    ) {
        return Map.of("message", exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleIllegalArgument(
            IllegalArgumentException exception
    ) {
        return Map.of("message", exception.getMessage());
    }

    @ExceptionHandler(BookingNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> handleBookingNotFound(
            BookingNotFoundException exception
    ) {
        return Map.of("message", exception.getMessage());
    }

    @ExceptionHandler(BookingInvalidStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleBookingNotConfirmable(
            BookingInvalidStateException exception
    ) {
        return Map.of("message", exception.getMessage());
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Map<String, String> handleEmailAlreadyExists(
            EmailAlreadyExistsException exception
    ) {
        return Map.of("message", exception.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Map<String, String> handleInvalidCredentials(
            InvalidCredentialsException exception
    ) {
        return Map.of(
                "message", exception.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleValidationException(
            MethodArgumentNotValidException exception
    ) {
        Map<String, String> errors = new HashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        return errors;
    }
}
