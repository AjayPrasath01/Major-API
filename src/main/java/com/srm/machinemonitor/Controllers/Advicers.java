package com.srm.machinemonitor.Controllers;

import com.srm.machinemonitor.CustomExceptions.UnauthorizedException;
import com.srm.machinemonitor.CustomExceptions.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class Advicers {

    Map<String, String> res;

    @ExceptionHandler(value = UserNotFoundException.class)
    public ResponseEntity<Map<String, String>> authErrors(WebRequest request, HttpServletResponse response, Exception ex){
        res = new HashMap<>();
        res.put("message", ex.getMessage());
        return new ResponseEntity<>(res, HttpStatus.NOT_FOUND);
    }
    
    @ExceptionHandler(value = UnauthorizedException.class)
    public ResponseEntity<Map<String, String>> unauthorizedException(WebRequest request, HttpServletResponse response, Exception ex){
        res = new HashMap<>();
        res.put("message", ex.getMessage());
        return new ResponseEntity<>(res, HttpStatus.UNAUTHORIZED);
    }
}
