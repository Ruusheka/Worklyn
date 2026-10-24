package com.worklyn.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.worklyn.common.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.time.Instant;

@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ObjectMapper mapper;
    public RestAuthenticationEntryPoint(ObjectMapper mapper){this.mapper=mapper;}
    @Override public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException ex) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value()); response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(response.getOutputStream(), new ApiErrorResponse(Instant.now(),401,"Unauthorized","UNAUTHENTICATED",
                "Authentication is required or the supplied token is invalid.",request.getRequestURI(),null));
    }
}
