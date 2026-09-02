package com.flashseat.flashseat_backend.exception;

public class SeatAlreadyExistsException extends RuntimeException{

    public SeatAlreadyExistsException(String message){
        super(message);
    }
}
