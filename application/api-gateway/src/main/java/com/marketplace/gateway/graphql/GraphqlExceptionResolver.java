package com.marketplace.gateway.graphql;

import com.marketplace.gateway.config.CorrelationIdFilter;
import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import org.slf4j.MDC;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.support.WebExchangeBindException;
import graphql.schema.DataFetchingEnvironment;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionException;

/**
 * Edge-layer error mapping (GraphQL).
 *
 * Keeps parity with REST ProblemDetails fields:
 * - extensions.code
 * - extensions.correlationId
 */
@Component
public class GraphqlExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        Throwable root = unwrap(ex);

        if (root instanceof IllegalArgumentException) {
            return error(env, ErrorType.BAD_REQUEST, "VALIDATION_ERROR", root.getMessage());
        }

        if (root instanceof MethodArgumentNotValidException || root instanceof WebExchangeBindException) {
            return error(env, ErrorType.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed");
        }

        if (root instanceof IllegalStateException) {
            return error(env, ErrorType.BAD_REQUEST, "CONFLICT", root.getMessage());
        }

        return error(env, ErrorType.INTERNAL_ERROR, "UNEXPECTED", "Unexpected error");
    }

    private GraphQLError error(DataFetchingEnvironment env, ErrorType type, String code, String message) {
        Map<String, Object> extensions = new LinkedHashMap<>();
        extensions.put("code", code);
        extensions.put("correlationId", correlationId(env));

        return GraphqlErrorBuilder
            .newError(env)
            .errorType(type)
            .message(Optional.ofNullable(message).orElse(code))
            .extensions(extensions)
            .build();
    }

    private String correlationId(DataFetchingEnvironment env) {
        Object fromContext = env.getGraphQlContext().getOrDefault(CorrelationIdGraphQlInterceptor.GRAPHQL_CONTEXT_KEY, null);
        if (fromContext instanceof String s && !s.isBlank()) {
            return s;
        }
        String fromMdc = MDC.get(CorrelationIdFilter.MDC_KEY);
        if (fromMdc != null && !fromMdc.isBlank()) {
            return fromMdc;
        }
        // Fallback: GraphQL execution may hop threads without MDC; ensure we still expose a correlation id.
        return UUID.randomUUID().toString();
    }

    private Throwable unwrap(Throwable ex) {
        if (ex instanceof CompletionException && ex.getCause() != null) {
            return unwrap(ex.getCause());
        }
        return ex;
    }
}
