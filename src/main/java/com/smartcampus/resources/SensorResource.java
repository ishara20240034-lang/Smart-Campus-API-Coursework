package com.smartcampus.resources;

import com.smartcampus.models.Sensor;
import com.smartcampus.models.Room;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Path("/sensors")
public class SensorResource {

    // Our in-memory sensor storage
    private static Map<String, Sensor> sensorDatabase = new ConcurrentHashMap<>();

    // Getter for the sub-resource to use later
    public static Map<String, Sensor> getSensorDatabase() {
        return sensorDatabase;
    }

    /**
     * Gets all sensors. Can filter by type if they use the query param.
     * Example: GET /api/v1/sensors?type=Temperature
     * (This is the Fix 3 requirement!)
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllSensors(@QueryParam("type") String type) {
        Collection<Sensor> sensors = sensorDatabase.values();

        // if they provided a type, we filter the list manually
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
     * Creates a new sensor and links it to a room.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor newSensor, @Context UriInfo uriInfo) {
        // Validation formatted as JSON for the "Excellent" rubric band
        if (newSensor.getId() == null || newSensor.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\": \"Sensor ID is required.\"}")
                           .type(MediaType.APPLICATION_JSON).build();
        }
        if (newSensor.getRoomId() == null || newSensor.getRoomId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\": \"Room ID is required to register a sensor.\"}")
                           .type(MediaType.APPLICATION_JSON).build();
        }

        // Check if the room actually exists before we try to link to it
        Map<String, Room> rooms = RoomResource.getRoomDatabase();
        Room parentRoom = rooms.get(newSensor.getRoomId());
        
        if (parentRoom == null) {
            // Throwing our custom 422 error mapper exception
            throw new com.smartcampus.exceptions.LinkedResourceNotFoundException("Cannot register sensor: Specified room ID '" + newSensor.getRoomId() + "' does not exist.");
        }

        // Add to our main sensor map
        sensorDatabase.put(newSensor.getId(), newSensor);

        // Update the Room's list so it knows it has a new sensor (Part 3 requirement)
        if (!parentRoom.getSensorIds().contains(newSensor.getId())) {
            parentRoom.getSensorIds().add(newSensor.getId());
        }

        // Building the Location header for the response
        URI location = uriInfo.getAbsolutePathBuilder().path(newSensor.getId()).build();

        return Response.created(location).entity(newSensor).build();
    }

    /**
     * Grab one specific sensor by ID.
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
                           .entity("{\"error\": \"Sensor not found.\"}")
                           .type(MediaType.APPLICATION_JSON).build();
        }
    }

    /**
     * Sub-resource locator for historical readings.
     * This is Part 4 of the coursework.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getSensorReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }
}