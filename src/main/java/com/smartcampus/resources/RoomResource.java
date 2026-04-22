package com.smartcampus.resources;

import com.smartcampus.models.Room;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Path("/rooms")
public class RoomResource {

    // storing rooms in memory. 
    // using ConcurrentHashMap because JAX-RS creates a new instance for every request, 
    // so this needs to be static and thread-safe.
    private static Map<String, Room> roomDatabase = new ConcurrentHashMap<>();

    // letting SensorResource access this to check if a room actually exists before adding a sensor
    public static Map<String, Room> getRoomDatabase() {
        return roomDatabase;
    }

    /**
     * Gets all the rooms we currently have saved.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllRooms() {
        /** --- TEMPORARY SABOTAGE FOR TASK 5.4 DEMO ---
        *if (true) {
            throw new RuntimeException("Simulated catastrophic failure for testing!");
        }
         --------------------------------------------*/
        
        Collection<Room> rooms = roomDatabase.values();
        return Response.ok(rooms).build();
    }

    /**
     * Creates a new room. 
     * Added UriInfo context to grab the path for the Location header.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON) 
    @Produces(MediaType.APPLICATION_JSON) 
    public Response createRoom(Room newRoom, @Context UriInfo uriInfo) {
        // basic check to make sure the ID isn't completely blank so it doesn't break our map
        if (newRoom.getId() == null || newRoom.getId().trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                           .entity("{\"error\": \"Room ID is required to create a room.\"}")
                           .type(MediaType.APPLICATION_JSON)
                           .build();
        }
        
        // save it to our database
        roomDatabase.put(newRoom.getId(), newRoom);
        
        // building the URI for the location header (needed to get the marks for the video demo!)
        URI location = uriInfo.getAbsolutePathBuilder().path(newRoom.getId()).build();
        
        // return 201 Created and attach the location header
        return Response.created(location).entity(newRoom).build();
    }

    /**
     * Grabs just one specific room using its ID from the URL.
     */
    @GET
    @Path("/{roomId}") 
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRoomById(@PathParam("roomId") String roomId) {
        Room room = roomDatabase.get(roomId);
        
        // if we found it, return it. otherwise, send a 404 error formatted as JSON.
        if (room != null) {
            return Response.ok(room).build(); 
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("{\"error\": \"Room not found.\"}")
                           .type(MediaType.APPLICATION_JSON)
                           .build(); 
        }
    }

    /**
     * Deletes a room, but only if it's empty.
     */
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = roomDatabase.get(roomId);
        
        // 1. check if it even exists first. Return 404 JSON if it doesn't. (Rubric requirement)
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("{\"error\": \"Room not found.\"}")
                           .type(MediaType.APPLICATION_JSON)
                           .build(); 
        }
        
        // 2. don't let them delete if there are still sensors attached to it
        if (!room.getSensorIds().isEmpty()) {
            // throws our custom exception so the ExceptionMapper can handle it and send a 409
            throw new com.smartcampus.exceptions.RoomNotEmptyException("Cannot delete: Room currently has active sensors assigned to it.");
        }
        
        // 3. safe to delete now
        roomDatabase.remove(roomId);
        return Response.noContent().build(); 
    }
}