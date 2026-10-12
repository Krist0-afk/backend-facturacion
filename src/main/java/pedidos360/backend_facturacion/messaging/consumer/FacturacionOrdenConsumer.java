package pedidos360.backend_facturacion.messaging.consumer;

import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Component;
import pedidos360.backend_facturacion.dto.OrdenCreadaEventDto;
import pedidos360.backend_facturacion.service.BoletaService;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class FacturacionOrdenConsumer {

    private final BoletaService boletaService;

    @RabbitListener(queues = "${app.rabbitmq.queues.facturacion-orden}")
    public void consumirOrdenCreada(OrdenCreadaEventDto evento, Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        Boolean redelivered = message.getMessageProperties().getRedelivered();

        log.info("[RabbitMQ Consumer] Recibido evento de orden para facturar: {} (deliveryTag={}, redelivered={})",
                evento, deliveryTag, redelivered);

        try {
            boletaService.emitirBoletaDesdeOrden(evento);
            // Confirmación explícita exitosa (ACK)
            channel.basicAck(deliveryTag, false);
            log.info("[RabbitMQ ACK] Mensaje confirmado exitosamente (deliveryTag={})", deliveryTag);

        } catch (IllegalArgumentException ex) {
            // Error de negocio NO recuperable -> NACK sin requeue (va directo a la DLQ)
            log.error("[RabbitMQ NACK -> DLQ] Datos inválidos en la orden. Enviando a DLQ sin reintento: {}", ex.getMessage());
            channel.basicNack(deliveryTag, false, false);

        } catch (TransientDataAccessException ex) {
            // Error recuperable: se reintenta 1 sola vez; si ya fue reentregado, se envía a DLQ
            if (Boolean.FALSE.equals(redelivered)) {
                log.warn("[RabbitMQ RETRY] Falla temporal de BD. Reencolando mensaje por única vez (deliveryTag={})", deliveryTag);
                channel.basicNack(deliveryTag, false, true);
            } else {
                log.error("[RabbitMQ NACK -> DLQ] Reintento agotado por falla de BD. Enviando a DLQ (deliveryTag={})", deliveryTag);
                channel.basicNack(deliveryTag, false, false);
            }

        } catch (Exception ex) {
            log.error("[RabbitMQ ERROR -> DLQ] Error inesperado procesando facturación. Enviando a DLQ: {}", ex.getMessage(), ex);
            channel.basicNack(deliveryTag, false, false);
        }
    }
}