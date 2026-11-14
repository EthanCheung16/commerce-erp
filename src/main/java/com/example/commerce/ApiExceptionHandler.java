package com.example.commerce;

import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.ResponseEntity;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler({IllegalArgumentException.class,ConstraintViolationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String,String> badInput(Exception error) { return Map.of("message",error.getMessage()); }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String,String>> statusError(ResponseStatusException error) {
        return ResponseEntity.status(error.getStatusCode()).body(Map.of("message",
                error.getReason() == null ? "请求未能完成" : error.getReason()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String,String> invalidForm(MethodArgumentNotValidException error) {
        return Map.of("message","请检查客户、地址、仓库及商品数量，所有必填项都需要有效值。");
    }
}
