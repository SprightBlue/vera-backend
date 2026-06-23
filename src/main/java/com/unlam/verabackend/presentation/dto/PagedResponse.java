package com.unlam.verabackend.presentation.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.function.Function;

@Data
@Builder
public class PagedResponse<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;

    public static <T, R> PagedResponse<R> fromPage(Page<T> page, Function<T, R> mapper) {
        return PagedResponse.<R>builder()
                .content(page.getContent().stream().map(mapper).toList())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}