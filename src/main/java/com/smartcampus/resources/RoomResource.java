package com.smartcampus.resources;

import com.smartcampus.models.Room;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Path("/rooms")
public class RoomResource {

    // In-memory data store for rooms. 
    // Note: Using ConcurrentHashMap for thread safety as JAX-RS resource instances are request-scoped.
    private static Map<String, Room> roomDatabase = new ConcurrentHashMap<>();

    /**
     * Retrieves all rooms currently registered in the system.
     * * @return A Response containing a JSON array of all Room objects.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllRooms() {
        Collection<Room> rooms = roomDatabase.values();
        return Response.ok(rooms).build();
    }

    /**
     * Creates a new room entry in the in-memory database.
     * * @param newRoom The Room object automatically deserialized from the JSON request payload.
     * @return 201 Created response if successful, or 400 Bad Request if validation fails.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON) 
    @Produces(MediaType.APPLICATION_JSON) 
    public Response createRoom(Room newRoom) {
        // Validate payload: ID cannot be null or empty
        if (newRoom.getId() == null || newRoom.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("Room ID is required to create a room.")
                           .build();
        }
        
        roomDatabase.put(newRoom.getId(), newRoom);
        
        return Response.status(Response.Status.CREATED).entity(newRoom).build();
    }

    /**
     * Retrieves a specific room by its unique identifier.
     * * @param roomId The ID extracted directly from the URL path.
     * @return 200 OK with the requested Room data, or a 404 Not Found status.
     */
    @GET
    @Path("/{roomId}") 
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = roomDatabase.get(roomId);
        
        if (room != null) {
            return Response.ok(room).build(); 
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Room not found.")
                           .build(); 
        }
    }
    /**
     * Deletes a specific room from the system.
     * @param roomId The unique identifier of the room to be removed.
     * @return 204 No Content upon successful deletion, or 404 Not Found.
     */
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        // Attempt to remove the room from the map
        Room removedRoom = roomDatabase.remove(roomId);
        
        if (removedRoom != null) {
            // 204 No Content is the standard REST response for a successful delete
            return Response.noContent().build(); 
        } else {
            // If the room wasn't in the map, return a 404
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Cannot delete: Room not found.")
                           .build(); 
        }
    }
    /**
     * Links a specific sensor to a specific room.
     * @param roomId The ID of the room.
     * @param sensorId The ID of the sensor to add.
     * @return 200 OK with the updated Room data, or 404 if the room isn't found.
     */
    @POST
    @Path("/{roomId}/sensors/{sensorId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addSensorToRoom(@PathParam("roomId") String roomId, @PathParam("sensorId") String sensorId) {
        
        // 1. Find the room in our database
        Room room = roomDatabase.get(roomId);
        
        // 2. If the room doesn't exist, return a 404 error
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Cannot link sensor: Room not found.")
                           .build();
        }
        
        // 3. Add the sensor ID to the room's list (if it isn't already there)
        if (!room.getSensorIds().contains(sensorId)) {
            room.getSensorIds().add(sensorId);
        }
        
        // 4. Return the updated room so the user can see the link
        return Response.ok(room).build();
    }
}
