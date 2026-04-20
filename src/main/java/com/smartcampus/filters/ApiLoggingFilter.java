package com.smartcampus.filters;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Intercepts all incoming API requests and outgoing responses to provide system observability.
 */
@Provider // This annotation tells GlassFish to automatically activate this filter
public class ApiLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    // Standard Java logger as requested by the coursework specification
    private static final Logger LOGGER = Logger.getLogger(ApiLoggingFilter.class.getName());

    /**
     * Executes BEFORE the request reaches the Resource controller.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo().getPath();
        
        LOGGER.info("--> INCOMING API REQUEST: " + method + " /" + path);
    }

    /**
     * Executes AFTER the Resource controller has finished processing, right before sending to client.
     */
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        int status = responseContext.getStatus();
        
        LOGGER.info("<-- OUTGOING API RESPONSE: Status " + status);
    }
}