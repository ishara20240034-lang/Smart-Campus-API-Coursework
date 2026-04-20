package com.smartcampus.resources;

import com.smartcampus.models.Sensor;
import com.smartcampus.models.Room;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Path("/sensors")
public class SensorResource {

    private static Map<String, Sensor> sensorDatabase = new ConcurrentHashMap<>();

    // Allow the sub-resource (SensorReadingResource) to access the sensor database
    public static Map<String, Sensor> getSensorDatabase() {
        return sensorDatabase;
    }

    /**
     * Retrieves all sensors. Includes an optional query parameter for filtering by type.
     * Example: GET /api/v1/sensors?type=CO2
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllSensors(@QueryParam("type") String type) {
        Collection<Sensor> sensors = sensorDatabase.values();

        if (type != null && !type.trim().isEmpty()) {
            List<Sensor> filteredSensors = new ArrayList<>();
            for (Sensor s : sensors) {
                if (type.equalsIgnoreCase(s.getType())) {
                    filteredSensors.add(s);
                }
            }
            return Response.ok(filteredSensors).build();
        }

        return Response.ok(sensors).build();
    }

    /**
     * Registers a new sensor and automatically links it to its parent room.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor newSensor) {
        if (newSensor.getId() == null || newSensor.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Sensor ID is required.").build();
        }
        if (newSensor.getRoomId() == null || newSensor.getRoomId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Room ID is required to register a sensor.").build();
        }

        Map<String, Room> rooms = RoomResource.getRoomDatabase();
        Room parentRoom = rooms.get(newSensor.getRoomId());
        
        if (parentRoom == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Cannot register sensor: Specified room does not exist.")
                           .build();
        }

        sensorDatabase.put(newSensor.getId(), newSensor);

        if (!parentRoom.getSensorIds().contains(newSensor.getId())) {
            parentRoom.getSensorIds().add(newSensor.getId());
        }

        return Response.status(Response.Status.CREATED).entity(newSensor).build();
    }

    /**
     * Retrieves a specific sensor by its unique identifier.
     */
    @GET
    @Path("/{sensorId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = sensorDatabase.get(sensorId);

        if (sensor != null) {
            return Response.ok(sensor).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Sensor not found.").build();
        }
    }

    /**
     * Part 4: Sub-Resource Locator Pattern
     * Delegates all requests for /sensors/{sensorId}/readings to the SensorReadingResource.
     * Note: This method does NOT have an HTTP annotation (like @GET or @POST).
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}