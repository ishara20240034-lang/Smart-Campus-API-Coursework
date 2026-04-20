package com.smartcampus.exceptions;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * The Global Safety Net: Catches ANY unhandled exceptions to prevent stack trace leaks.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    @Override
    public Response toResponse(Throwable exception) {
        
        // 1. Log the actual error to the server console so developers can debug it privately
        System.err.println("CRITICAL UNHANDLED EXCEPTION INTERCEPTED:");
        exception.printStackTrace();

        // 2. Return a safe, generic JSON error to the client so they don't see the stack trace
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                       .entity("{\"error\": \"An unexpected internal server error occurred. Please contact the system administrator.\"}")
                       .type("application/json")
                       .build();
    }
}