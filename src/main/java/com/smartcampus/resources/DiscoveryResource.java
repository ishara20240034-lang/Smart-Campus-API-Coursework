package com.smartcampus.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

@Path("/") 
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getApiMetadata() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("version", "1.0");
        
        // Changed "adminContact" to "contact" to align with standard discovery patterns
        // and updated the email 
        metadata.put("contact", "admin@smartcampus.ac.uk");

        Map<String, String> resources = new HashMap<>();
        resources.put("rooms", "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");

        metadata.put("resources", resources);
        
        // Using Response.ok(metadata) is perfect because JAX-RS 
        // handles the JSON serialization for us automatically.
        return Response.ok(metadata).build();
    }
}