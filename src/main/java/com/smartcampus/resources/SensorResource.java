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

    /**
     * Retrieves all sensors. Includes an optional query parameter for filtering by type.
     * Example: GET /api/v1/sensors?type=CO2
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllSensors(@QueryParam("type") String type) {
        Collection<Sensor> sensors = sensorDatabase.values();

        // Part 3 Filter Logic: If the user provided a type, filter the list
        if (type != null && !type.trim().isEmpty()) {
            List<Sensor> filteredSensors = new ArrayList<>();
            for (Sensor s : sensors) {
                if (type.equalsIgnoreCase(s.getType())) {
                    filteredSensors.add(s);
                }
            }
            return Response.ok(filteredSensors).build();
        }

        // If no filter is provided, return all sensors
        return Response.ok(sensors).build();
    }

    /**
     * Registers a new sensor and automatically links it to its parent room.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor newSensor) {
        // 1. Basic Data Validation
        if (newSensor.getId() == null || newSensor.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Sensor ID is required.").build();
        }
        if (newSensor.getRoomId() == null || newSensor.getRoomId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Room ID is required to register a sensor.").build();
        }

        // 2. Cross-Resource Validation (Does the room actually exist?)
        Map<String, Room> rooms = RoomResource.getRoomDatabase();
        Room parentRoom = rooms.get(newSensor.getRoomId());
        
        if (parentRoom == null) {
            // Note: The rubric asks for a 422 error here in Part 5, we are using 400 temporarily 
            // until we build the custom Exception Mappers!
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Cannot register sensor: Specified room does not exist.")
                           .build();
        }

        // 3. Save the sensor to the database
        sensorDatabase.put(newSensor.getId(), newSensor);

        // 4. Automatically link it! Add this sensor's ID to the Room's internal list
        if (!parentRoom.getSensorIds().contains(newSensor.getId())) {
            parentRoom.getSensorIds().add(newSensor.getId());
        }

        return Response.status(Response.Status.CREATED).entity(newSensor).build();
    }

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
}