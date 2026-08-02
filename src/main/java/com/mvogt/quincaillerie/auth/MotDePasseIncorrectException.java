package com.mvogt.quincaillerie.auth;

public class MotDePasseIncorrectException extends RuntimeException {

    public MotDePasseIncorrectException(String message) {
        super(message);
    }
}
