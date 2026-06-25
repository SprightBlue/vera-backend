package com.unlam.verabackend.infrastructure.provider;
import com.unlam.verabackend.domain.port.out.GeoLocationProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Service
public class OpenStreetMapProvider implements GeoLocationProvider {
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String URL = "https://nominatim.openstreetmap.org/reverse?lat={lat}&lon={lon}&format=json";

    public String getAddressFromCoords(BigDecimal lat, BigDecimal lon) {
        log.info("Geocodificando coordenadas: lat={}, lon={}", lat, lon);
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "VeraBackend/1.0 (contacto@tuapp.com)");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            Map<String, Object> response = restTemplate.exchange(
                    URL,
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<Map<String, Object>>() {},
                    lat,
                    lon
            ).getBody();

            return (response != null && response.containsKey("display_name"))
                    ? (String) response.get("display_name")
                    : "Ubicación desconocida";

        } catch (Exception e) {
            log.error("Error en API Nominatim: {}", e.getMessage());
            return "Ubicación desconocida";
        }
    }
}