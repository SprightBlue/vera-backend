package com.unlam.verabackend.infrastructure.provider;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.unlam.verabackend.domain.port.out.GeocodingProvider;
import lombok.Getter;
import lombok.Setter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OsmGeocodingAdapter implements GeocodingProvider {

    private final RestTemplate restTemplate;

    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/reverse?format=json&lat={lat}&lon={lon}";
    private static final String USER_AGENT_VALUE = "VeraBackendApp/1.0 (soporte-vera@unlam.edu.ar)";
    private static final String DEFAULT_FALLBACK_ADDRESS = "Dirección no disponible";

    @Override
    public String getAddressFromCoordinates(BigDecimal latitude, BigDecimal longitude) {
        log.info("Infrastructure Adapter: Consultando geocodificación inversa en OpenStreetMap para [Lat: {}, Lng: {}]", latitude, longitude);

        try {
            HttpEntity<Void> requestEntity = buildHttpEntityWithHeaders();
            Map<String, String> uriVariables = Map.of(
                    "lat", latitude.toString(),
                    "lon", longitude.toString()
            );

            log.debug("Infrastructure Adapter: Transmitiendo HTTP GET saliente hacia el servidor de Nominatim.");
            ResponseEntity<OsmResponse> response = restTemplate.exchange(
                    NOMINATIM_URL,
                    HttpMethod.GET,
                    requestEntity,
                    OsmResponse.class,
                    uriVariables
            );

            return processOsmResponse(response);

        } catch (Exception e) {
            log.error("Infrastructure Exception: Error de red o timeout al comunicar con OpenStreetMap. Motivo: {}", e.getMessage(), e);
            return DEFAULT_FALLBACK_ADDRESS;
        }
    }

    private HttpEntity<Void> buildHttpEntityWithHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.USER_AGENT, USER_AGENT_VALUE);
        return new HttpEntity<>(headers);
    }

    private String processOsmResponse(ResponseEntity<OsmResponse> response) {
        OsmResponse body = response.getBody();

        if (body != null && body.getDisplayName() != null) {
            String sanitizedAddress = body.getDisplayName().trim();
            log.info("Infrastructure Adapter: Coordenadas resueltas exitosamente -> '{}'", sanitizedAddress);
            return sanitizedAddress;
        }

        log.warn("Infrastructure Adapter: La API de OSM devolvió un body vacío o sin el atributo 'display_name' esperado.");
        return DEFAULT_FALLBACK_ADDRESS;
    }

    @Getter
    @Setter
    private static class OsmResponse {
        @JsonProperty("display_name")
        private String displayName;
    }
}