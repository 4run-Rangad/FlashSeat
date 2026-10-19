package com.flashseat.flashseat_backend.exception;

public class SeatAlreadyLockedException extends RuntimeException {
    public SeatAlreadyLockedException(String message) {
        super(message);
    }
}
