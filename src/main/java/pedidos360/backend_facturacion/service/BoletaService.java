package pedidos360.backend_facturacion.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pedidos360.backend_facturacion.dto.BoletaEmitidaEventDto;
import pedidos360.backend_facturacion.dto.OrdenCreadaEventDto;
import pedidos360.backend_facturacion.entity.Boleta;
import pedidos360.backend_facturacion.repository.BoletaRepository;
import pedidos360.backend_facturacion.messaging.producer.FacturacionProducer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BoletaService {

    private final BoletaRepository boletaRepository;
    private final FacturacionProducer facturacionProducer;

    @Transactional
    public Boleta emitirBoletaDesdeOrden(OrdenCreadaEventDto evento) {
        if (evento.getOrdenId() == null || evento.getOrdenId() <= 0) {
            throw new IllegalArgumentException("OrdenId inválido para facturación: " + evento.getOrdenId());
        }
        if (evento.getTotal() == null || evento.getTotal().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto total debe ser mayor a 0 para emitir boleta SII");
        }

        // Si ya existe boleta para esta orden, la retorna sin duplicar
        return boletaRepository.findByOrdenId(evento.getOrdenId())
                .orElseGet(() -> {
                    BigDecimal total = evento.getTotal();
                    BigDecimal neto = total.divide(BigDecimal.valueOf(1.19), 2, RoundingMode.HALF_UP);
                    BigDecimal iva = total.subtract(neto);

                    String folio = "SII-39-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

                    Boleta nuevaBoleta = Boleta.builder()
                            .folioSii(folio)
                            .ordenId(evento.getOrdenId())
                            .usuarioEmail(evento.getUsuarioEmail() != null ? evento.getUsuarioEmail() : "cliente@pedidos360.cl")
                            .montoNeto(neto)
                            .iva(iva)
                            .montoTotal(total)
                            .estado("EMITIDA")
                            .fechaEmision(LocalDateTime.now())
                            .build();

                    Boleta guardada = boletaRepository.save(nuevaBoleta);
                    log.info("[SII] Boleta Electrónica generada con éxito: Folio={} para Orden=#{}", folio, evento.getOrdenId());

                    // Publicar evento de boleta emitida en RabbitMQ
                    facturacionProducer.publicarBoletaEmitida(BoletaEmitidaEventDto.builder()
                            .boletaId(guardada.getId())
                            .folioSii(guardada.getFolioSii())
                            .ordenId(guardada.getOrdenId())
                            .usuarioEmail(guardada.getUsuarioEmail())
                            .montoNeto(guardada.getMontoNeto())
                            .iva(guardada.getIva())
                            .montoTotal(guardada.getMontoTotal())
                            .fechaEmision(guardada.getFechaEmision())
                            .build());

                    return guardada;
                });
    }

    public List<Boleta> listarTodas() {
        return boletaRepository.findAll();
    }

    public List<Boleta> listarPorEmail(String email) {
        return boletaRepository.findByUsuarioEmailOrderByFechaEmisionDesc(email);
    }
}