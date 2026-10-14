package pedidos360.backend_facturacion.messaging.consumer;

import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class FacturacionDlqConsumer {

    @RabbitListener(queues = "${app.rabbitmq.queues.facturacion-dlq}", containerFactory = "dlqListenerContainerFactory")
    public void procesarMensajeEnDlq(Message message, Channel channel) throws IOException {
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        String payload = new String(message.getBody(), StandardCharsets.UTF_8);

        log.error("==================================================================");
        log.error("[DLQ AUDITORÍA] Mensaje no entregado capturado en Dead Letter Queue");
        log.error("Cola origen / Routing Key : {}", message.getMessageProperties().getReceivedRoutingKey());
        log.error("Payload fallido           : {}", payload);
        log.error("Headers                   : {}", message.getMessageProperties().getHeaders());
        log.error("==================================================================");

        channel.basicAck(deliveryTag, false);
    }
}