package com.learning.reactive.exception;

public class CustomException extends Throwable{

    String message;

    public CustomException(String message){
        this.message = message;
    }
}
