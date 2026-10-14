package pedidos360.backend_facturacion.controller;



import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pedidos360.backend_facturacion.dto.OrdenCreadaEventDto;
import pedidos360.backend_facturacion.entity.Boleta;
import pedidos360.backend_facturacion.service.BoletaService;
import pedidos360.backend_facturacion.messaging.producer.FacturacionProducer;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/facturacion")
@RequiredArgsConstructor
public class BoletaController {

    private final BoletaService boletaService;
    private final FacturacionProducer facturacionProducer;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "backend-facturacion"));
    }

    // Boletas del usuario logueado (el correo sale del JWT)
    @GetMapping("/boletas/mias")
    public ResponseEntity<List<Boleta>> misBoletas(Authentication authentication) {
        return ResponseEntity.ok(boletaService.listarPorEmail(authentication.getName()));
    }

    // Solo Admin (ver SecurityConfig)
    @GetMapping("/boletas")
    public ResponseEntity<List<Boleta>> listarBoletas(@RequestParam(required = false) String email) {
        if (email != null && !email.isBlank()) {
            return ResponseEntity.ok(boletaService.listarPorEmail(email));
        }
        return ResponseEntity.ok(boletaService.listarTodas());
    }

    @PostMapping("/boletas/emitir")
    public ResponseEntity<Boleta> emitirManual(@Valid @RequestBody OrdenCreadaEventDto dto) {
        return ResponseEntity.ok(boletaService.emitirBoletaDesdeOrden(dto));
    }

    @PostMapping("/boletas/simular-evento-rabbit")
    public ResponseEntity<Map<String, Object>> simularEventoRabbit(@RequestBody OrdenCreadaEventDto dto) {
        facturacionProducer.publicarOrdenParaFacturar(dto);
        return ResponseEntity.accepted().body(Map.of(
                "mensaje", "Evento enviado a RabbitMQ para facturación asíncrona",
                "ordenId", dto.getOrdenId(),
                "total", dto.getTotal()
        ));
    }
}