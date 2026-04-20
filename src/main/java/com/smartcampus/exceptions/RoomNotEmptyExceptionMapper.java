package com.smartcampus.exceptions;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        // Return a 409 Conflict with a JSON error message
        return Response.status(Response.Status.CONFLICT)
                       .entity("{\"error\": \"" + exception.getMessage() + "\"}")
                       .type("application/json")
                       .build();
    }
}