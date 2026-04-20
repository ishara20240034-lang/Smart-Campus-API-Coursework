package com.smartcampus.exceptions;

/**
 * Custom exception thrown when attempting to delete a room that still contains sensors.
 */
public class RoomNotEmptyException extends RuntimeException {
    public RoomNotEmptyException(String message) {
        super(message);
    }
}