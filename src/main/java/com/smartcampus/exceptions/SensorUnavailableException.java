package com.smartcampus.exceptions;

/**
 * Custom exception thrown when attempting to interact with a sensor that is not ACTIVE.
 */
public class SensorUnavailableException extends RuntimeException {
    public SensorUnavailableException(String message) {
        super(message);
    }
}