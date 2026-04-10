package com.marketplace.gateway.api;

import com.marketplace.gateway.config.CorrelationIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * Edge-layer error mapping (REST). Domain/app errors are intentionally preserved as typed HTTP errors.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest req) {
        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", ex.getMessage(), req);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ProblemDetail> handleIllegalState(IllegalStateException ex, HttpServletRequest req) {
        return problem(HttpStatus.CONFLICT, "CONFLICT", ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleBeanValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setType(URI.create("urn:problem:validation"));
        pd.setTitle("Validation error");
        pd.setDetail("Request validation failed");
        pd.setInstance(URI.create(req.getRequestURI()));
        pd.setProperty("code", "VALIDATION_ERROR");
        pd.setProperty("correlationId", MDC.get(CorrelationIdFilter.MDC_KEY));

        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(fe -> fieldErrors.put(fe.getField(), fe.getDefaultMessage()));
        pd.setProperty("errors", fieldErrors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpected(Exception ex, HttpServletRequest req) {
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "UNEXPECTED", "Unexpected error", req);
    }

    private ResponseEntity<ProblemDetail> problem(
        HttpStatus status,
        String code,
        String detail,
        HttpServletRequest req
    ) {
        ProblemDetail pd = ProblemDetail.forStatus(status);
        pd.setType(URI.create("urn:problem:" + code.toLowerCase()));
        pd.setTitle(status.getReasonPhrase());
        pd.setDetail(detail);
        pd.setInstance(URI.create(req.getRequestURI()));
        pd.setProperty("code", code);
        pd.setProperty("correlationId", MDC.get(CorrelationIdFilter.MDC_KEY));
        return ResponseEntity.status(status).body(pd);
    }
}

