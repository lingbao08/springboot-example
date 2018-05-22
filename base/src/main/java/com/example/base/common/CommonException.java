package com.example.base.common;

public class CommonException extends Exception {

    private String message;

    private Throwable cause;

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Throwable getCause() {
        return cause;
    }

    public CommonException(String message) {
        this.message = message;
    }

    public CommonException(String message, Throwable cause) {
        this.message = message;
        this.cause = cause;
    }
}
