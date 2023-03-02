package com.srm.machinemonitor.CustomExceptions;

public class UserNotFoundException extends org.springframework.security.core.AuthenticationException{

    public UserNotFoundException(String message) {
        super(message);
    }
}
