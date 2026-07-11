package com.unlam.verabackend.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.domain.Page;
import java.util.List;
import java.util.function.Function;

@Data
@Builder
@Schema(description = "Estructura genérica unificada para respuestas paginadas del sistema basadas en Spring Pageable")
public class PagedResponse<T> {

    @Schema(description = "Colección de elementos del tipo solicitado correspondientes a la página actual", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<T> content;

    @Schema(description = "Número de la página actual recuperada (Basado en índice 0)", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private int pageNumber;

    @Schema(description = "Cantidad máxima de registros configurados por página", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private int pageSize;

    @Schema(description = "Cantidad total de elementos existentes en toda la base de datos bajo los filtros aplicados", example = "45", requiredMode = Schema.RequiredMode.REQUIRED)
    private long totalElements;

    @Schema(description = "Cantidad total de páginas disponibles calculadas", example = "5", requiredMode = Schema.RequiredMode.REQUIRED)
    private int totalPages;

    @Schema(description = "Indicador booleano que determina si es la última página disponible del conjunto", example = "false", requiredMode = Schema.RequiredMode.REQUIRED)
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