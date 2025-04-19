package com.ledok.spring.security.orderservice.advice;

public class TimeIncorrectException extends RuntimeException {
    public TimeIncorrectException(String message) {
        super(message);
    }
}
