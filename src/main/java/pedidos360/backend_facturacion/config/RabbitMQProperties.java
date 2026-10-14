package pedidos360.backend_facturacion.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.rabbitmq")
public class RabbitMQProperties {

    private Exchanges exchanges = new Exchanges();
    private Queues queues = new Queues();
    private RoutingKeys routingKeys = new RoutingKeys();

    @Data
    public static class Exchanges {
        private String ordenesTopic;
        private String facturacionDirect;
        private String dlx;
    }

    @Data
    public static class Queues {
        private String facturacionOrden;
        private String facturacionDlq;
    }

    @Data
    public static class RoutingKeys {
        private String ordenCreada;
        private String ordenCreadaExact;
        private String boletaEmitida;
        private String dlq;
    }
}