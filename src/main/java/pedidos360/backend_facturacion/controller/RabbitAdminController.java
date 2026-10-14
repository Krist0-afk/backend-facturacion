package pedidos360.backend_facturacion.controller;



import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.AmqpException;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import pedidos360.backend_facturacion.dto.CrearBindingRequest;
import pedidos360.backend_facturacion.dto.CrearColaRequest;
import pedidos360.backend_facturacion.dto.CrearExchangeRequest;
import pedidos360.backend_facturacion.service.RabbitAdminService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@RestController
@RequestMapping("/api/v1/facturacion/rabbit-admin")
@RequiredArgsConstructor
public class RabbitAdminController {

    // Mismo formato que en los DTO
    private static final String NOMBRE_VALIDO = "^[a-zA-Z0-9._-]{3,100}$";
    private static final String MENSAJE_NOMBRE = "Nombre inválido: entre 3 y 100 caracteres (letras, números, '.', '-', '_')";

    private final RabbitAdminService rabbitAdminService;

    @PostMapping("/queues")
    public ResponseEntity<Map<String, Object>> crearCola(@Valid @RequestBody CrearColaRequest request) {
        String nombre = rabbitAdminService.crearCola(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "mensaje", "Cola creada exitosamente en RabbitMQ",
                "queue", nombre,
                "durable", request.isDurable()
        ));
    }

    @DeleteMapping("/queues/{name}")
    public ResponseEntity<Map<String, Object>> eliminarCola(
            @PathVariable @Pattern(regexp = NOMBRE_VALIDO, message = MENSAJE_NOMBRE) String name) {
        boolean eliminada = rabbitAdminService.eliminarCola(name);
        return ResponseEntity.ok(Map.of(
                "mensaje", eliminada ? "Cola eliminada exitosamente" : "La cola no existía o no pudo eliminarse",
                "queue", name,
                "eliminada", eliminada
        ));
    }

    @GetMapping("/queues/{name}")
    public ResponseEntity<Map<String, Object>> estadoCola(
            @PathVariable @Pattern(regexp = NOMBRE_VALIDO, message = MENSAJE_NOMBRE) String name) {
        Properties props = rabbitAdminService.consultarEstadoCola(name);
        if (props == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "La cola '" + name + "' no existe en RabbitMQ"));
        }
        return ResponseEntity.ok(Map.of("queue", name, "properties", props));
    }

    @PostMapping("/exchanges")
    public ResponseEntity<Map<String, Object>> crearExchange(@Valid @RequestBody CrearExchangeRequest request) {
        rabbitAdminService.crearExchange(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "mensaje", "Exchange creado exitosamente",
                "exchange", request.getName(),
                "type", request.getType()
        ));
    }

    @DeleteMapping("/exchanges/{name}")
    public ResponseEntity<Map<String, Object>> eliminarExchange(
            @PathVariable @Pattern(regexp = NOMBRE_VALIDO, message = MENSAJE_NOMBRE) String name) {
        boolean eliminado = rabbitAdminService.eliminarExchange(name);
        return ResponseEntity.ok(Map.of(
                "exchange", name,
                "eliminado", eliminado
        ));
    }

    @PostMapping("/bindings")
    public ResponseEntity<Map<String, Object>> crearBinding(@Valid @RequestBody CrearBindingRequest request) {
        rabbitAdminService.crearBinding(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "mensaje", "Binding creado exitosamente",
                "exchange", request.getExchangeName(),
                "queue", request.getQueueName(),
                "routingKey", request.getRoutingKey()
        ));
    }

    @DeleteMapping("/bindings")
    public ResponseEntity<Map<String, Object>> eliminarBinding(@Valid @RequestBody CrearBindingRequest request) {
        rabbitAdminService.eliminarBinding(request);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Binding eliminado exitosamente",
                "exchange", request.getExchangeName(),
                "queue", request.getQueueName(),
                "routingKey", request.getRoutingKey()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidaciones(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> errores.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of(
                "error", "Parámetros de entrada inválidos",
                "detalles", errores
        ));
    }

    // Validación de los nombres que vienen en la URL
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Map<String, Object>> manejarValidacionRuta(HandlerMethodValidationException ex) {
        List<String> errores = ex.getAllErrors().stream().map(e -> e.getDefaultMessage()).toList();
        return ResponseEntity.badRequest().body(Map.of(
                "error", "Parámetros de entrada inválidos",
                "detalles", errores
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> manejarArgumentoInvalido(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
    }

    // Por ejemplo un binding a una cola que no existe
    @ExceptionHandler(AmqpException.class)
    public ResponseEntity<Map<String, Object>> manejarErrorRabbit(AmqpException ex) {
        Throwable causa = NestedExceptionUtils.getMostSpecificCause(ex);
        return ResponseEntity.badRequest().body(Map.of(
                "error", "RabbitMQ rechazó la operación",
                "detalle", causa.getMessage() != null ? causa.getMessage() : ex.getMessage()
        ));
    }
}
