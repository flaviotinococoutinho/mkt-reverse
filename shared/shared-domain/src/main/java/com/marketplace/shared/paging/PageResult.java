package com.marketplace.shared.paging;

import java.util.List;

/**
 * Domain-friendly pagination result.
 *
 * Avoids leaking Spring Data types into domain/application layers.
 */
public record PageResult<T>(
    List<T> items,
    int page,
    int size,
    long totalElements
) {

    public PageResult {
        if (items == null) {
            throw new IllegalArgumentException("items cannot be null");
        }
        if (page < 0) {
            throw new IllegalArgumentException("page must be >= 0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size must be > 0");
        }
        if (totalElements < 0) {
            throw new IllegalArgumentException("totalElements must be >= 0");
        }
    }

    public int totalPages() {
        if (totalElements == 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalElements / (double) size);
    }

    public <R> PageResult<R> map(java.util.function.Function<T, R> mapper) {
        if (mapper == null) {
            throw new IllegalArgumentException("mapper cannot be null");
        }
        return new PageResult<>(items.stream().map(mapper).toList(), page, size, totalElements);
    }

    public static <T> PageResult<T> empty() {
        return new PageResult<>(java.util.Collections.emptyList(), 0, 1, 0);
    }
}
