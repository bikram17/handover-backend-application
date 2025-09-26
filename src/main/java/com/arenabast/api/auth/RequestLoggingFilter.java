package com.arenabast.api.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Enumeration;

@Component
public class RequestLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        StringBuilder sb = new StringBuilder();
        sb.append("📥 Incoming Request: ")
                .append(request.getMethod()).append(" ")
                .append(request.getRequestURI());

        // Query Params
        if (request.getQueryString() != null) {
            sb.append("?").append(request.getQueryString());
        }

        // Headers
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            sb.append("\n   ").append(header).append(": ").append(request.getHeader(header));
        }

        logger.info(sb.toString());

        filterChain.doFilter(request, response);
    }
}