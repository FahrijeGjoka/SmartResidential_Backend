package com.smartresidential.backend.middleware;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class LoggingMiddleware extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(LoggingMiddleware.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            @NonNull HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        long start = System.currentTimeMillis();

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String ip = request.getRemoteAddr();
        String userAgent = request.getHeader("User-Agent");

        logger.info("Incoming Request -> Method: {} | URI: {} | IP: {} | User-Agent: {}",
                method, uri, ip, userAgent);

        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            logger.error("Error during request processing: {}", e.getMessage());
            throw e;
        }

        long duration = System.currentTimeMillis() - start;

        logger.info("Response -> Status: {} | Time: {} ms",
                response.getStatus(), duration);
    }
}