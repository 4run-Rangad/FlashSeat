package com.flashseat.flashseat_backend.exception;

public class BookingInvalidStateException extends RuntimeException {
    public BookingInvalidStateException(String message) {
        super(message);
    }
}
