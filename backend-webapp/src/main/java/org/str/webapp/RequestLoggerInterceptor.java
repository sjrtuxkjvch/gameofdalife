package org.str.webapp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@PreMatching
public class RequestLoggerInterceptor implements ContainerRequestFilter, ContainerResponseFilter {
    private static final Logger logger = LoggerFactory.getLogger(RequestLoggerInterceptor.class);

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        logger.debug("Request received on path {}:", requestContext.getUriInfo().getAbsolutePath());
        logger.debug("Method: " + requestContext.getRequest().getMethod());
        try {
            String json = IOUtils.toString(requestContext.getEntityStream(), StandardCharsets.UTF_8);
            logger.debug("Content: " + json);
            // replace input stream for Jersey as we've already read it
            InputStream in = IOUtils.toInputStream(json, StandardCharsets.UTF_8);
            requestContext.setEntityStream(in);
        } catch (IOException ex) {
            logger.warn("Exception while request logging", ex);
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext)
            throws IOException {
        logger.debug("Response to Request {} - {}:", requestContext.getUriInfo().getAbsolutePath(),
                requestContext.getRequest().getMethod());
        logger.debug("Header: {}", responseContext.getHeaders());
        logger.debug("Entity: {}", responseContext.getEntity());
    }
}
