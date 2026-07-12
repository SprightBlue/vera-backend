package com.unlam.verabackend.presentation.controller;

import com.unlam.verabackend.domain.model.Analysis;
import com.unlam.verabackend.domain.model.RiskLevel;
import com.unlam.verabackend.domain.port.in.AnalyzeContentUseCase;
import com.unlam.verabackend.domain.port.in.ChatUseCase;
import com.unlam.verabackend.domain.port.in.ManageAnalysisUseCase;
import com.unlam.verabackend.infrastructure.entity.User;
import com.unlam.verabackend.presentation.dto.PagedResponse;
import com.unlam.verabackend.presentation.dto.AnalysisDetailResponse;
import com.unlam.verabackend.presentation.dto.AnalysisResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/analysis")
@RequiredArgsConstructor
@Tag(name = "Análisis de Contenido", description = "Endpoints para el procesamiento, auditoría y derivación de análisis de seguridad aplicados a mensajes, links y archivos multimedia")
public class AnalysisController {

    private final AnalyzeContentUseCase analyzeContentUseCase;
    private final ManageAnalysisUseCase manageAnalysisUseCase;
    private final ChatUseCase chatUseCase;

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(
            summary = "Analizar contenido (Mensajes, Enlaces o Archivos)",
            description = "Procesa el texto enviado o el archivo multimedia adjunto para emitir un veredicto de seguridad heurístico. Se requiere al menos un parámetro de entrada (text o file).",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Análisis completado e informe técnico detallado generado con éxito",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AnalysisDetailResponse.class))
                    ),
                    @ApiResponse(responseCode = "400", description = "Solicitud inválida - Payload vacío o datos malformados", content = @Content),
                    @ApiResponse(responseCode = "415", description = "Tipo de archivo multimedia no soportado", content = @Content),
                    @ApiResponse(responseCode = "500", description = "Error interno durante la inspección de la amenaza", content = @Content)
            }
    )
    public ResponseEntity<AnalysisDetailResponse> analyze(
            @AuthenticationPrincipal @Parameter(hidden = true) User user,
            @RequestParam(value = "text", required = false) @Parameter(description = "Contenido de texto o URL sospechosa a evaluar", example = "Haga clic aquí urgente para recuperar su cuenta de homebanking.") String text,
            @RequestParam(value = "file", required = false) @Parameter(description = "Archivo multimedia o binario (.mp3, .jpg, .pdf) bajo sospecha") MultipartFile file,
            @RequestParam(value = "source") @Parameter(description = "Origen o canal desde donde se dispara la solicitud", example = "MOBILE", schema = @Schema(allowableValues = {"WEB", "MOBILE"})) String source
    ) {
        log.info("REST Request: POST - Solicitando análisis de contenido por [{}] desde el origen [{}]", user.getEmail(), source);
        validateAnalysisPayload(text, file);

        var result = analyzeContentUseCase.execute(user.getEmail(), text, file, source);

        log.debug("REST Response: Análisis procesado exitosamente. Empaquetando resultado detallado.");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(AnalysisDetailResponse.fromDomain(result));
    }

    @PostMapping("/chat/{analysisId}")
    @Operation(
            summary = "Inicializar chat con IA desde un análisis",
            description = "Deriva un reporte de análisis de riesgo específico para abrir una nueva sesión de conversación contextual con la IA y evacuar dudas de seguridad.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Sesión de chat contextual creada exitosamente. Devuelve el UUID del nuevo chat.",
                            content = @Content(mediaType = "application/json", schema = @Schema(type = "string", format = "uuid", example = "9b1deb4d-3b7d-4bad-9bdd-2b0d7b3dcb6d"))
                    ),
                    @ApiResponse(responseCode = "404", description = "El ID de análisis proporcionado no existe", content = @Content)
            }
    )
    public ResponseEntity<UUID> initializeChatFromAnalysis(
            @AuthenticationPrincipal @Parameter(hidden = true) User user,
            @PathVariable @Parameter(description = "ID del análisis del cual se quiere heredar el contexto", example = "123e4567-e89b-12d3-a456-426614174000") UUID analysisId
    ) {
        log.info("REST Request: POST - Usuario [{}] solicita derivación a chat contextual desde el análisis ID [{}]", user.getEmail(), analysisId);

        UUID newChatId = chatUseCase.createChat(user.getEmail(), analysisId);

        return ResponseEntity.status(HttpStatus.CREATED).body(newChatId);
    }

    @GetMapping
    @Operation(
            summary = "Obtener historial de análisis paginado",
            description = "Recupera la lista histórica de análisis solicitados por el usuario. Soporta filtros dinámicos por severidad de riesgo y cadenas de búsqueda por coincidencia textual.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Página de registros históricos obtenida correctamente",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = PagedResponse.class))
                    )
            }
    )
    public ResponseEntity<PagedResponse<AnalysisResponse>> getAnalysisHistory(
            @AuthenticationPrincipal @Parameter(hidden = true) User user,
            @RequestParam(value = "riskLevel", required = false) @Parameter(description = "Filtro opcional por nivel de severidad", example = "HIGH") RiskLevel riskLevel,
            @RequestParam(value = "search", required = false) @Parameter(description = "Término de búsqueda para filtrar por título o resumen", example = "phishing") String search,
            @RequestParam(value = "page", defaultValue = "0") @Parameter(description = "Número de página a recuperar (Basado en índice 0)", example = "0") int page
    ) {
        log.info("REST Request: GET - Solicitando historial de análisis filtrado para [{}]", user.getEmail());

        Page<Analysis> historyPage = manageAnalysisUseCase.getAnalysisHistory(user.getEmail(), riskLevel, search, page);

        return ResponseEntity.ok(convertToPagedAnalysisResponse(historyPage));
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Consultar desglose detallado de un análisis",
            description = "Devuelve el informe forense completo (patrones encontrados, recomendaciones de mitigación e índices de riesgo precisos) mediante su identificador único.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Detalle del análisis encontrado",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = AnalysisDetailResponse.class))
                    ),
                    @ApiResponse(responseCode = "403", description = "Acceso denegado - El análisis no pertenece al usuario solicitante", content = @Content),
                    @ApiResponse(responseCode = "404", description = "Análisis no encontrado", content = @Content)
            }
    )
    public ResponseEntity<AnalysisDetailResponse> getAnalysisDetail(
            @AuthenticationPrincipal @Parameter(hidden = true) User user,
            @PathVariable @Parameter(description = "ID del análisis consultado", example = "123e4567-e89b-12d3-a456-426614174000") UUID id
    ) {
        log.info("REST Request: GET - Consultando desglose del análisis ID [{}] por el operador [{}]", id, user.getEmail());

        Analysis analysis = manageAnalysisUseCase.getAnalysisDetail(id, user.getEmail());
        return ResponseEntity.ok(AnalysisDetailResponse.fromDomain(analysis));
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar un registro de análisis",
            description = "Remueve físicamente el registro de análisis e informe técnico asociado del historial del usuario.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Análisis eliminado con éxito (No Content)", content = @Content),
                    @ApiResponse(responseCode = "403", description = "Operación denegada - Permisos insuficientes", content = @Content),
                    @ApiResponse(responseCode = "404", description = "El ID especificado no corresponde a ningún registro", content = @Content)
            }
    )
    public ResponseEntity<Void> deleteAnalysis(
            @AuthenticationPrincipal @Parameter(hidden = true) User user,
            @PathVariable @Parameter(description = "ID del análisis que se desea dar de baja", example = "123e4567-e89b-12d3-a456-426614174000") UUID id
    ) {
        log.info("REST Request: DELETE - Solicitando eliminación del análisis ID [{}] por el operador [{}]", id, user.getEmail());

        manageAnalysisUseCase.deleteAnalysis(id, user.getEmail());
        return ResponseEntity.noContent().build();
    }

    private void validateAnalysisPayload(String text, MultipartFile file) {
        if ((text == null || text.isBlank()) && (file == null || file.isEmpty())) {
            log.warn("Payload Validation Exception: Estructura vacía. Se requiere texto o archivo adjunto válidos.");
            throw new IllegalArgumentException("Debe proporcionar al menos un texto o un archivo para analizar.");
        }
    }

    private PagedResponse<AnalysisResponse> convertToPagedAnalysisResponse(Page<Analysis> historyPage) {
        log.debug("REST Response: Formateando {} registros de análisis históricos a estructura paginada.", historyPage.getNumberOfElements());
        return PagedResponse.fromPage(historyPage, AnalysisResponse::fromDomain);
    }
}