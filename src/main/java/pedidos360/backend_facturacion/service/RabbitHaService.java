package pedidos360.backend_facturacion.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.util.List;
import java.util.Map;

// Mantiene cada cola replicada en los dos nodos de RabbitMQ (policy ha-todas), revisando cada 30 s
@Slf4j
@Service
public class RabbitHaService {

    private static final String POLICY = "/api/policies/%2F/ha-todas";
    private static final Map<String, Object> DEFINICION = Map.of(
            "pattern", ".*",
            "apply-to", "queues",
            "priority", 0,
            "definition", Map.of("ha-mode", "all", "ha-sync-mode", "automatic"));

    private final List<String> urls;
    private final RestClient http;

    public RabbitHaService(@Value("${app.rabbitmq.management.urls}") List<String> urls,
                           @Value("${spring.rabbitmq.username}") String usuario,
                           @Value("${spring.rabbitmq.password}") String clave) {
        this.urls = urls;
        this.http = RestClient.builder()
                .defaultHeaders(headers -> headers.setBasicAuth(usuario, clave))
                .build();
    }

    @Scheduled(initialDelay = 10_000, fixedDelay = 30_000)
    public void verificar() {
        // Si un nodo está caído se usa el otro
        for (String url : urls) {
            try {
                verificar(url);
                return;
            } catch (RestClientException e) {
                log.debug("[RabbitHA] {} no responde: {}", url, e.getMessage());
            }
        }
        log.warn("[RabbitHA] Ningún nodo respondió a la API de administración de RabbitMQ");
    }

    private void verificar(String url) {
        if (!existePolicy(url)) {
            aplicarPolicy(url);
            log.info("[RabbitHA] Policy ha-todas aplicada: cada cola se replica en todos los nodos");
            return;
        }

        long nodosActivos = get(url + "/api/nodes?columns=name,running").stream()
                .filter(nodo -> Boolean.TRUE.equals(nodo.get("running")))
                .count();
        if (nodosActivos < 2) {
            return; // con un solo nodo arriba no hay dónde replicar
        }

        List<Object> sinReplica = get(url + "/api/queues/%2F?columns=name,slave_nodes").stream()
                .filter(cola -> !(cola.get("slave_nodes") instanceof List<?> replicas) || replicas.isEmpty())
                .map(cola -> cola.get("name"))
                .toList();
        if (!sinReplica.isEmpty()) {
            log.warn("[RabbitHA] Colas sin réplica {}: se vuelve a aplicar la policy", sinReplica);
            http.delete().uri(URI.create(url + POLICY)).retrieve().toBodilessEntity();
            aplicarPolicy(url);
        }
    }

    private boolean existePolicy(String url) {
        try {
            http.get().uri(URI.create(url + POLICY)).retrieve().toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }

    private void aplicarPolicy(String url) {
        http.put().uri(URI.create(url + POLICY))
                .contentType(MediaType.APPLICATION_JSON)
                .body(DEFINICION)
                .retrieve()
                .toBodilessEntity();
    }

    private List<Map<String, Object>> get(String uri) {
        return http.get().uri(URI.create(uri)).retrieve().body(new ParameterizedTypeReference<>() {});
    }
}
