package com.smartcampus.resources;

import com.smartcampus.models.Sensor;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Path("/sensors")
public class SensorResource {

    // In-memory data store for sensors.
    // Must be static to survive across multiple request lifecycles.
    private static Map<String, Sensor> sensorDatabase = new ConcurrentHashMap<>();

    /**
     * Retrieves all sensors currently registered in the system.
     * @return A Response containing a JSON array of all Sensor objects.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllSensors() {
        Collection<Sensor> sensors = sensorDatabase.values();
        return Response.ok(sensors).build();
    }

    /**
     * Registers a new sensor into the system.
     * @param newSensor The Sensor object deserialized from the request payload.
     * @return 201 Created response if successful, or 400 Bad Request if validation fails.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor newSensor) {
        // Validation: Ensure ID is provided
        if (newSensor.getId() == null || newSensor.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Sensor ID is required.")
                           .build();
        }

        // Save the sensor to the map
        sensorDatabase.put(newSensor.getId(), newSensor);

        return Response.status(Response.Status.CREATED).entity(newSensor).build();
    }

    /**
     * Retrieves a specific sensor by its unique identifier.
     * @param sensorId The ID extracted directly from the URL path.
     * @return 200 OK with Sensor data, or a 404 Not Found status.
     */
    @GET
    @Path("/{sensorId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = sensorDatabase.get(sensorId);

        if (sensor != null) {
            return Response.ok(sensor).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Sensor not found.")
                           .build();
        }
    }
}