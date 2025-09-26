package com.arenabast.api.controller;

import com.arenabast.api.dto.ResponseWrapper;
import com.arenabast.api.exception.DataValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ApiRestHandler {
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseWrapper<Object>> handleRuntimeException(RuntimeException ex) {
        ResponseWrapper<Object> error = new ResponseWrapper<>(500, "Opps! something went wrong", null);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseWrapper<Object>> handleGenericException(Exception ex) {
        ResponseWrapper<Object> error = new ResponseWrapper<>(500, "Internal server error", null);
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DataValidationException.class)
    public ResponseEntity<ResponseWrapper<Object>> handleDataValidationException(DataValidationException ex) {
        ResponseWrapper<Object> error = new ResponseWrapper<>(400, ex.getMessage(), null);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
