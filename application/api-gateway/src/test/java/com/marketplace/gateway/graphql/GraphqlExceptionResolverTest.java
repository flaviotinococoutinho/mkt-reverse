package com.marketplace.gateway.graphql;

import graphql.GraphQLError;
import graphql.GraphQLContext;
import graphql.execution.ExecutionStepInfo;
import graphql.execution.ResultPath;
import graphql.language.Field;
import graphql.schema.DataFetchingEnvironment;
import org.junit.jupiter.api.Test;
import org.springframework.graphql.execution.ErrorType;

import java.util.Map;
import java.util.concurrent.CompletionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GraphqlExceptionResolverTest {

    private final TestableGraphqlExceptionResolver resolver = new TestableGraphqlExceptionResolver();

    @Test
    void shouldMapIllegalArgumentToValidationErrorWithCorrelationFromContext() {
        DataFetchingEnvironment env = mockEnv("corr-123");

        GraphQLError error = resolver.resolve(new IllegalArgumentException("bad payload"), env);

        assertEquals(ErrorType.BAD_REQUEST, error.getErrorType());
        assertEquals("bad payload", error.getMessage());
        assertExtension(error, "code", "VALIDATION_ERROR");
        assertExtension(error, "correlationId", "corr-123");
    }

    @Test
    void shouldUnwrapCompletionException() {
        DataFetchingEnvironment env = mockEnv("corr-456");

        GraphQLError error = resolver.resolve(
            new CompletionException(new IllegalArgumentException("wrapped validation")),
            env
        );

        assertEquals(ErrorType.BAD_REQUEST, error.getErrorType());
        assertEquals("wrapped validation", error.getMessage());
        assertExtension(error, "code", "VALIDATION_ERROR");
        assertExtension(error, "correlationId", "corr-456");
    }

    @Test
    void shouldMapIllegalStateToConflict() {
        DataFetchingEnvironment env = mockEnv("corr-789");

        GraphQLError error = resolver.resolve(new IllegalStateException("state conflict"), env);

        assertEquals(ErrorType.BAD_REQUEST, error.getErrorType());
        assertEquals("state conflict", error.getMessage());
        assertExtension(error, "code", "CONFLICT");
    }

    @Test
    void shouldMapUnknownErrorToUnexpectedAndGenerateCorrelationIdWhenMissing() {
        DataFetchingEnvironment env = mockEnv(null);

        GraphQLError error = resolver.resolve(new RuntimeException("boom"), env);

        assertEquals(ErrorType.INTERNAL_ERROR, error.getErrorType());
        assertEquals("Unexpected error", error.getMessage());
        assertExtension(error, "code", "UNEXPECTED");

        Object correlationId = error.getExtensions().get("correlationId");
        assertNotNull(correlationId);
        assertFalse(correlationId.toString().isBlank());
    }

    private DataFetchingEnvironment mockEnv(String correlationId) {
        DataFetchingEnvironment env = mock(DataFetchingEnvironment.class);
        GraphQLContext context = correlationId == null
            ? GraphQLContext.newContext().build()
            : GraphQLContext.newContext()
                .of(CorrelationIdGraphQlInterceptor.GRAPHQL_CONTEXT_KEY, correlationId)
                .build();

        when(env.getGraphQlContext()).thenReturn(context);
        when(env.getField()).thenReturn(Field.newField("testField").build());

        ExecutionStepInfo executionStepInfo = mock(ExecutionStepInfo.class);
        when(executionStepInfo.getPath()).thenReturn(ResultPath.parse("/testField"));
        when(env.getExecutionStepInfo()).thenReturn(executionStepInfo);

        return env;
    }

    private static void assertExtension(GraphQLError error, String key, String expected) {
        Map<String, Object> extensions = error.getExtensions();
        assertNotNull(extensions);
        assertEquals(expected, extensions.get(key));
    }

    private static class TestableGraphqlExceptionResolver extends GraphqlExceptionResolver {
        GraphQLError resolve(Throwable ex, DataFetchingEnvironment env) {
            return super.resolveToSingleError(ex, env);
        }
    }
}
