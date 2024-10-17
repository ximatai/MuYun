package net.ximatai.muyun.http;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Provider
//@ApplicationScoped
public class LogFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOG = LoggerFactory.getLogger(LogFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // 记录请求信息
        LOG.info("HTTP Request: {} {}", requestContext.getMethod(), requestContext.getUriInfo().getRequestUri());
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        // 记录响应信息
        LOG.info("HTTP Response: {} {} -> Status: {}", requestContext.getMethod(), requestContext.getUriInfo().getRequestUri(), responseContext.getStatus());
    }
}
