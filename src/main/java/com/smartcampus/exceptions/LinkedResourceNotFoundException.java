package com.smartcampus.exceptions;

/**
 * Custom exception thrown when a resource tries to link to a parent resource that does not exist.
 */
public class LinkedResourceNotFoundException extends RuntimeException {
    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}