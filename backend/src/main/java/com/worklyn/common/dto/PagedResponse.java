package com.worklyn.common.dto;

import org.springframework.data.domain.Page;
import java.util.List;

public record PagedResponse<T>(List<T> content, int pageNumber, int pageSize,
                               long totalElements, int totalPages, boolean last) {
    public static <T> PagedResponse<T> from(Page<T> page) {
        return new PagedResponse<>(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages(), page.isLast());
    }

    public static <T> PagedResponse<T> of(Page<T> page) {
        return from(page);
    }
}
