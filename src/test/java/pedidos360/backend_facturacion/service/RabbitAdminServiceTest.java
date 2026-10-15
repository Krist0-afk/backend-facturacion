package pedidos360.backend_facturacion.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import pedidos360.backend_facturacion.dto.CrearColaRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RabbitAdminServiceTest {

    @Mock
    private RabbitAdmin rabbitAdmin;

    @InjectMocks
    private RabbitAdminService service;

    @Test
    void creaLaColaConSuDeadLetterExchange() {
        CrearColaRequest req = new CrearColaRequest();
        req.setName("prueba.queue");
        req.setDeadLetterExchange("prueba.dlx");
        req.setDeadLetterRoutingKey("prueba.dlq.key");
        when(rabbitAdmin.declareQueue(org.mockito.ArgumentMatchers.any(Queue.class))).thenReturn("prueba.queue");

        String nombre = service.crearCola(req);

        ArgumentCaptor<Queue> captor = ArgumentCaptor.forClass(Queue.class);
        verify(rabbitAdmin).declareQueue(captor.capture());
        Queue cola = captor.getValue();
        assertThat(nombre).isEqualTo("prueba.queue");
        assertThat(cola.isDurable()).isTrue();
        assertThat(cola.getArguments())
                .containsEntry("x-dead-letter-exchange", "prueba.dlx")
                .containsEntry("x-dead-letter-routing-key", "prueba.dlq.key");
    }

    @Test
    void eliminaLaCola() {
        when(rabbitAdmin.deleteQueue("prueba.queue")).thenReturn(true);

        assertThat(service.eliminarCola("prueba.queue")).isTrue();
    }
}
