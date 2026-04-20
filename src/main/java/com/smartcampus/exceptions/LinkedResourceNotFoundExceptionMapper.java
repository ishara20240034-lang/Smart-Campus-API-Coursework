package com.smartcampus.exceptions;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class LinkedResourceNotFoundExceptionMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        // 422 Unprocessable Entity means the JSON was formatted correctly, 
        // but contained invalid logical data (like a fake Room ID).
        return Response.status(422)
                       .entity("{\"error\": \"" + exception.getMessage() + "\"}")
                       .type("application/json")
                       .build();
    }
}