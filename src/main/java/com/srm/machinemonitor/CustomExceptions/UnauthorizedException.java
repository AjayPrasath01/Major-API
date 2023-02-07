package com.srm.machinemonitor.CustomExceptions;

public class UnauthorizedException extends Exception{
    public UnauthorizedException(String message) {
        super(message);
    }
}
