package com.learning.reactive;

public class CustomException extends Throwable{

    String message;

    public CustomException(String message){
        this.message = message;
    }
}
