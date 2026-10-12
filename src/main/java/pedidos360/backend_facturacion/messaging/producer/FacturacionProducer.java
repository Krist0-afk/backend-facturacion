package pedidos360.backend_facturacion.messaging.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import pedidos360.backend_facturacion.config.RabbitMQProperties;
import pedidos360.backend_facturacion.dto.BoletaEmitidaEventDto;
import pedidos360.backend_facturacion.dto.OrdenCreadaEventDto;

@Slf4j
@Component
@RequiredArgsConstructor
public class FacturacionProducer {

    private final RabbitTemplate rabbitTemplate;
    private final RabbitMQProperties props;

    public void publicarBoletaEmitida(BoletaEmitidaEventDto evento) {
        log.info("[RabbitMQ Producer] Publicando evento boleta.emitida para Orden #{} con Folio {}",
                evento.getOrdenId(), evento.getFolioSii());
        rabbitTemplate.convertAndSend(
                props.getExchanges().getFacturacionDirect(),
                props.getRoutingKeys().getBoletaEmitida(),
                evento
        );
    }

    public void publicarOrdenParaFacturar(OrdenCreadaEventDto evento) {
        log.info("[RabbitMQ Producer] Publicando evento de orden creada hacia Topic Exchange: Orden #{}",
                evento.getOrdenId());
        rabbitTemplate.convertAndSend(
                props.getExchanges().getOrdenesTopic(),
                props.getRoutingKeys().getOrdenCreadaExact(),
                evento
        );
    }
}