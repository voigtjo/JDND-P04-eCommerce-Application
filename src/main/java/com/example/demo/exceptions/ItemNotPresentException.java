package com.example.demo.exceptions;

public class ItemNotPresentException extends Exception {
    public ItemNotPresentException(String message) {
        super(message);
    }
}
