package pedidos360.backend_facturacion.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Service;
import pedidos360.backend_facturacion.dto.CrearBindingRequest;
import pedidos360.backend_facturacion.dto.CrearColaRequest;
import pedidos360.backend_facturacion.dto.CrearExchangeRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Service
@RequiredArgsConstructor
public class RabbitAdminService {

    private final RabbitAdmin rabbitAdmin;

    public String crearCola(CrearColaRequest req) {
        Map<String, Object> args = new HashMap<>();
        if (req.getDeadLetterExchange() != null && !req.getDeadLetterExchange().isBlank()) {
            args.put("x-dead-letter-exchange", req.getDeadLetterExchange());
        }
        if (req.getDeadLetterRoutingKey() != null && !req.getDeadLetterRoutingKey().isBlank()) {
            args.put("x-dead-letter-routing-key", req.getDeadLetterRoutingKey());
        }

        Queue queue = new Queue(req.getName(), req.isDurable(), req.isExclusive(), req.isAutoDelete(), args);
        String result = rabbitAdmin.declareQueue(queue);
        log.info("[RabbitAdminService] Cola creada/declarada: {}", req.getName());
        return result != null ? result : req.getName();
    }

    public boolean eliminarCola(String queueName) {
        boolean deleted = rabbitAdmin.deleteQueue(queueName);
        log.info("[RabbitAdminService] Solicitud de eliminación de cola '{}', resultado={}", queueName, deleted);
        return deleted;
    }

    public void purgarCola(String queueName) {
        rabbitAdmin.purgeQueue(queueName, false);
        log.info("[RabbitAdminService] Cola '{}' purgada correctamente", queueName);
    }

    public Properties consultarEstadoCola(String queueName) {
        return rabbitAdmin.getQueueProperties(queueName);
    }

    public void crearExchange(CrearExchangeRequest req) {
        Exchange exchange = switch (req.getType().toLowerCase()) {
            case "direct" -> new DirectExchange(req.getName(), req.isDurable(), false);
            case "topic" -> new TopicExchange(req.getName(), req.isDurable(), false);
            case "fanout" -> new FanoutExchange(req.getName(), req.isDurable(), false);
            default -> throw new IllegalArgumentException("Tipo de exchange no soportado: " + req.getType());
        };
        rabbitAdmin.declareExchange(exchange);
        log.info("[RabbitAdminService] Exchange '{}' de tipo '{}' creado", req.getName(), req.getType());
    }

    public boolean eliminarExchange(String exchangeName) {
        boolean deleted = rabbitAdmin.deleteExchange(exchangeName);
        log.info("[RabbitAdminService] Exchange '{}' eliminado={}", exchangeName, deleted);
        return deleted;
    }

    public void crearBinding(CrearBindingRequest req) {
        Binding binding = new Binding(
                req.getQueueName(),
                Binding.DestinationType.QUEUE,
                req.getExchangeName(),
                req.getRoutingKey(),
                null
        );
        rabbitAdmin.declareBinding(binding);
        log.info("[RabbitAdminService] Binding creado: Exchange '{}' -> Cola '{}' [key='{}']",
                req.getExchangeName(), req.getQueueName(), req.getRoutingKey());
    }
}