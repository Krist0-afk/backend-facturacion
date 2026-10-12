package pedidos360.backend_facturacion.controller;



import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import pedidos360.backend_facturacion.dto.CrearBindingRequest;
import pedidos360.backend_facturacion.dto.CrearColaRequest;
import pedidos360.backend_facturacion.dto.CrearExchangeRequest;
import pedidos360.backend_facturacion.service.RabbitAdminService;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@RestController
@RequestMapping("/api/v1/facturacion/rabbit-admin")
@RequiredArgsConstructor
public class RabbitAdminController {

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
    public ResponseEntity<Map<String, Object>> eliminarCola(@PathVariable @NotBlank String name) {
        if (name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "El nombre de la cola no puede ser vacío"));
        }
        boolean eliminada = rabbitAdminService.eliminarCola(name);
        return ResponseEntity.ok(Map.of(
                "mensaje", eliminada ? "Cola eliminada exitosamente" : "La cola no existía o no pudo eliminarse",
                "queue", name,
                "eliminada", eliminada
        ));
    }

    @GetMapping("/queues/{name}")
    public ResponseEntity<Map<String, Object>> estadoCola(@PathVariable String name) {
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
    public ResponseEntity<Map<String, Object>> eliminarExchange(@PathVariable String name) {
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> manejarValidaciones(MethodArgumentNotValidException ex) {
        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err -> errores.put(err.getField(), err.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of(
                "error", "Parámetros de entrada inválidos",
                "detalles", errores
        ));
    }
}