package com.marketplace.gateway.api;

import com.marketplace.gateway.api.schema.SourcingSchema;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.Map;

/**
 * Global exception handler using table-driven approach.
 * 
 * Reduces complexity from O(n) if-else to O(1) lookup.
 * 
 * Object Calisthenics:
 * - No magic numbers
 * - Single responsibility
 * - Immutable mappings
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // Table-driven exception mapping
    private static final Map<Class<? extends Exception>, ExceptionMapper> EXCEPTION_MAPPERS = Map.of(
            IllegalArgumentException.class, new ExceptionMapper(
                    HttpStatus.BAD_REQUEST,
                    "VALIDATION_ERROR"
            ),
            IllegalStateException.class, new ExceptionMapper(
                    HttpStatus.CONFLICT,
                    "CONFLICT"
            ),
            MethodArgumentNotValidException.class, new ExceptionMapper(
                    HttpStatus.BAD_REQUEST,
                    "VALIDATION_ERROR"
            )
    );

    private static final ExceptionMapper DEFAULT_MAPPER = new ExceptionMapper(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "UNEXPECTED"
    );

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest req
    ) {
        var mapper = EXCEPTION_MAPPERS.getOrDefault(
                MethodArgumentNotValidException.class,
                DEFAULT_MAPPER
        );

        var pd = mapper.toProblemDetail();
        pd.setType(URI.create("urn:problem:validation"));
        pd.setTitle("Validation error");
        pd.setDetail("Request validation failed");
        pd.setInstance(URI.create(req.getRequestURI()));

        var fieldErrors = new java.util.HashMap<String, String>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> fieldErrors.put(fe.getField(), fe.getDefaultMessage()));
        pd.setProperty("errors", fieldErrors);
        pd.setProperty("correlationId", MDC.get("correlationId"));

        return ResponseEntity.status(mapper.status()).body(pd);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handle(Exception ex, HttpServletRequest req) {
        var mapper = EXCEPTION_MAPPERS.getOrDefault(
                ex.getClass(),
                DEFAULT_MAPPER
        );

        var message = (ex.getMessage() != null) 
                ? ex.getMessage() 
                : mapper.title();

        return mapper.toProblem(message, req).toResponse();
    }

    /**
     * Immutable exception mapper - Value Object
     */
    record ExceptionMapper(HttpStatus status, String code) {

        ResponseEntity<ProblemDetail> toResponse() {
            return ResponseEntity.status(status())
                    .body(toProblemDetail());
        }

        ProblemDetail toProblemDetail() {
            var pd = ProblemDetail.forStatus(status());
            pd.setType(URI.create("urn:problem:" + code().toLowerCase()));
            pd.setTitle(status().getReasonPhrase());
            pd.setProperty("code", code());
            return pd;
        }

        ProblemDetail toProblem(String detail, HttpServletRequest req) {
            var pd = toProblemDetail();
            pd.setDetail(detail);
            pd.setInstance(URI.create(req.getRequestURI()));
            pd.setProperty("correlationId", MDC.get("correlationId"));
            return pd;
        }
    }
}