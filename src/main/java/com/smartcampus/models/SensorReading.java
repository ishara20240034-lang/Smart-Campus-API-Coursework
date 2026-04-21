package com.smartcampus.models;

import java.util.UUID;

/**
 * Model for sensor history readings.
 * Added unique ID and timestamp to track data points over time.
 */
public class SensorReading {
    
    private String id;        
    private long timestamp;   
    private double value;     

    /**
     * Default constructor. 
     * We initialize the ID and timestamp here so every reading is unique by default.
     */
    public SensorReading() {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
    }

    /**
     * Overloaded constructor for manual creation in our logic.
     */
    public SensorReading(double value) {
        this(); // This calls the default constructor above to set ID and timestamp!
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