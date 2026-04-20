package com.smartcampus.models;

import java.util.UUID;

public class SensorReading {
    
    private String id;        // Unique reading event ID
    private long timestamp;   // Epoch time (ms) when the reading was captured
    private double value;     // The actual metric value recorded

    /**
     * Default constructor for JAX-RS JSON deserialization.
     */
    public SensorReading() {
        // Automatically generate a unique ID and current timestamp if not provided
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }

    public SensorReading(String id, long timestamp, double value) {
        this.id = id;
        this.timestamp = timestamp;
        this.value = value;
    }

    // --- GETTERS AND SETTERS ---

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }
}