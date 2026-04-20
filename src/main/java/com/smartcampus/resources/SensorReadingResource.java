package com.smartcampus.resources;

import com.smartcampus.models.Sensor;
import com.smartcampus.models.SensorReading;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Notice this class does NOT have a @Path annotation at the top.
 * It is dynamically instantiated and routed by the SensorResource locator.
 */
public class SensorReadingResource {

    private String sensorId;

    // In-memory database for historical readings. 
    // Key = sensorId, Value = List of reading events.
    private static Map<String, List<SensorReading>> readingDatabase = new ConcurrentHashMap<>();

    // Constructor dynamically receives the ID from the parent locator
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    /**
     * Retrieves the entire history of readings for this specific sensor.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReadings() {
        // 1. Verify the parent sensor actually exists
        Sensor sensor = SensorResource.getSensorDatabase().get(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Cannot fetch history: Sensor not found.")
                           .build();
        }

        // 2. Return the history log (or an empty list if no readings exist yet)
        List<SensorReading> history = readingDatabase.getOrDefault(sensorId, new ArrayList<>());
        return Response.ok(history).build();
    }

    /**
     * Appends a new reading to the history log AND updates the parent sensor.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addReading(SensorReading newReading) {
        
        Map<String, Sensor> sensorDb = SensorResource.getSensorDatabase();
        Sensor parentSensor = sensorDb.get(sensorId);
        
        // 1. Verify parent sensor exists
        if (parentSensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Cannot add reading: Sensor not found.")
                           .build();
        }

        // 2. Part 5.3: State Constraint Check (403 Forbidden)
        // If the sensor is in MAINTENANCE or OFFLINE, block the reading!
        if ("MAINTENANCE".equalsIgnoreCase(parentSensor.getStatus()) || "OFFLINE".equalsIgnoreCase(parentSensor.getStatus())) {
            throw new com.smartcampus.exceptions.SensorUnavailableException(
                "Sensor '" + sensorId + "' is currently in " + parentSensor.getStatus() + " mode and cannot accept new readings."
            );
        }

        // 3. Add the new reading to the historical database
        readingDatabase.putIfAbsent(sensorId, new ArrayList<>());
        readingDatabase.get(sensorId).add(newReading);

        // 4. THE REQUIRED SIDE EFFECT: Update the parent sensor's current value!
        parentSensor.setCurrentValue(newReading.getValue());
        sensorDb.put(sensorId, parentSensor); // Save the updated sensor state

        return Response.status(Response.Status.CREATED).entity(newReading).build();
    }
}