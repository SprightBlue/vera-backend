package com.unlam.verabackend.infrastructure.provider;

import com.unlam.verabackend.domain.port.out.GeocodingProvider;
import lombok.Data;
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

@Component
@RequiredArgsConstructor
@Slf4j
public class OsmGeocodingAdapter implements GeocodingProvider {

    private final RestTemplate restTemplate;
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/reverse?format=json&lat={lat}&lon={lon}";

    @Override
    public String getAddressFromCoordinates(BigDecimal latitude, BigDecimal longitude) {
        log.info("Iniciando geocodificación inversa mediante OSM Nominatim para coordenadas: [Lat: {}, Lng: {}]", latitude, longitude);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "VeraBackendApp/1.0 (soporte-vera@unlam.edu.ar)");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            Map<String, String> params = Map.of(
                    "lat", latitude.toString(),
                    "lon", longitude.toString()
            );

            log.debug("Enviando petición HTTP GET a OSM: {}", NOMINATIM_URL);
            ResponseEntity<OsmResponse> response = restTemplate.exchange(
                    NOMINATIM_URL,
                    HttpMethod.GET,
                    entity,
                    OsmResponse.class,
                    params
            );

            if (response.getBody() != null && response.getBody().getDisplay_name() != null) {
                String address = response.getBody().getDisplay_name();
                log.info("Geocodificación exitosa. Dirección obtenida: '{}'", address);
                return address;
            }

            log.warn("OSM devolvió una respuesta vacía o sin atributo 'display_name' para las coordenadas dadas.");
            return "Dirección no disponible";

        } catch (Exception e) {
            log.error("Falló la comunicación con el proveedor externo de OpenStreetMap debido a: {}", e.getMessage(), e);
            return "Ubicación en tiempo real";
        }
    }

    @Data
    private static class OsmResponse {
        private String display_name;
    }
}