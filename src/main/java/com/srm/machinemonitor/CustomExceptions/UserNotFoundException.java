package com.srm.machinemonitor.CustomExceptions;

public class UserNotFoundException extends Exception{

    public UserNotFoundException(String message) {
        super(message);
    }
}
