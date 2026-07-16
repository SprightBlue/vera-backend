package com.unlam.verabackend.presentation.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.unlam.verabackend.domain.model.DashboardData;
import com.unlam.verabackend.domain.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Respuesta unificada del Dashboard. Dependiendo del rol solicitado, se omitirán campos mediante la política NON_NULL.")
public class DashboardResponse {

    @Schema(description = "Listado de los 3 análisis más recientes del usuario. Exclusivo para el rol PROTECTED (será null para CARER).")
    private List<AnalysisResponse> top3Analysis;

    @Schema(description = "Listado de las 3 alertas activas más recientes. Exclusivo para el rol CARER (será null para PROTECTED).")
    private List<AlertsResponse> top3Alerts;

    @Schema(description = "Cantidad total de análisis realizados durante la última semana", example = "14", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long analysisCountSince;

    @Schema(description = "Cantidad total de alertas generadas durante la última semana", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long alertsCountSince;

    @Schema(description = "Cantidad total de alertas que fueron resueltas durante la última semana", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long resolvedAlertsCountSince;

    @Schema(description = "Detalles del último chat modificado/actualizado del usuario. Puede ser nulo si no registra actividad.")
    private ChatSessionResponse latestUpdatedChat;

    @Schema(description = "Último contacto de confianza añadido al sistema (mapeado dinámicamente con la contraparte).")
    private TrustContactResponse latestTrustContact;

    public static DashboardResponse fromDomain(DashboardData data, Role role) {
        if (data == null) return null;

        DashboardResponseBuilder builder = DashboardResponse.builder()
                .analysisCountSince(data.getAnalysisCountSince())
                .alertsCountSince(data.getAlertsCountSince())
                .resolvedAlertsCountSince(data.getResolvedAlertsCountSince())
                .latestUpdatedChat(data.getLatestUpdatedChat() != null ? ChatSessionResponse.fromDomain(data.getLatestUpdatedChat()) : null)
                .latestTrustContact(TrustContactResponse.fromEntity(data.getLatestTrustContact(), role));

        if (role == Role.PROTECTED) {
            builder.top3Analysis(
                    data.getTop3Analysis() != null
                            ? data.getTop3Analysis().stream().map(AnalysisResponse::fromDomain).toList()
                            : List.of()
            );
        } else if (role == Role.CARER) {
            builder.top3Alerts(
                    data.getTop3Alerts() != null
                            ? data.getTop3Alerts().stream().map(d -> AlertsResponse.fromDomain(d, role)).toList()
                            : List.of()
            );
        }

        return builder.build();
    }
}