package com.smartcampus.exceptions;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class SensorUnavailableExceptionMapper implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException exception) {
        // 403 Forbidden: The server understood the request, but refuses to authorize it 
        // due to the current state of the resource.
        return Response.status(Response.Status.FORBIDDEN)
                       .entity("{\"error\": \"" + exception.getMessage() + "\"}")
                       .type("application/json")
                       .build();
    }
}