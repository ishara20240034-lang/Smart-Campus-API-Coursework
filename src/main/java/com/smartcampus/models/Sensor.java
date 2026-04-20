package com.smartcampus.models;

public class Sensor {
    
    private String id;        // e.g., "TEMP-001"
    private String type;      // e.g., "Temperature", "Motion", "Light"
    private double value;     // e.g., 22.5 (Celsius) or 1.0 (Motion detected)
    private boolean active;   // true if the sensor is online and working

    /**
     * Default empty constructor.
     * Strictly required by JAX-RS for automatic JSON deserialisation.
     */
    public Sensor() {
    }

    /**
     * Parameterized constructor for easy instantiation in the backend.
     */
    public Sensor(String id, String type, double value, boolean active) {
        this.id = id;
        this.type = type;
        this.value = value;
        this.active = active;
    }

    // --- GETTERS AND SETTERS ---

    public String getId() { 
        return id; 
    }
    
    public void setId(String id) { 
        this.id = id; 
    }

    public String getType() { 
        return type; 
    }
    
    public void setType(String type) { 
        this.type = type; 
    }

    public double getValue() { 
        return value; 
    }
    
    public void setValue(double value) { 
        this.value = value; 
    }

    public boolean isActive() { 
        return active; 
    }
    
    public void setActive(boolean active) { 
        this.active = active; 
    }
}