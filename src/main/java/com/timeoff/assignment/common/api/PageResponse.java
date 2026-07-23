package com.timeoff.assignment.common.api;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Stable, serialization-friendly pagination envelope. Prefer this over returning Spring's
 * {@code Page} directly, whose JSON structure is not part of a stable API contract.
 */
@Schema(name = "PageResponse", description = "Paged collection of results")
public record PageResponse<T>(

        @Schema(description = "Items on the current page") List<T> content,
        @Schema(description = "Zero-based page number", example = "0") int page,
        @Schema(description = "Page size", example = "20") int size,
        @Schema(description = "Total number of matching elements", example = "42") long totalElements,
        @Schema(description = "Total number of pages", example = "3") int totalPages,
        @Schema(description = "Whether this is the last page", example = "false") boolean last
) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}
