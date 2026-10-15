package pedidos360.backend_facturacion.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

// Validaciones de entrada de la API de administración de RabbitMQ
class RabbitAdminValidacionTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    private static CrearColaRequest cola(String nombre) {
        CrearColaRequest req = new CrearColaRequest();
        req.setName(nombre);
        return req;
    }

    @Test
    void nombreDeColaValido() {
        assertThat(validator.validate(cola("prueba.admin.queue"))).isEmpty();
    }

    @Test
    void nombreDeColaVacioOCortoEsRechazado() {
        assertThat(validator.validate(cola(""))).isNotEmpty();
        assertThat(validator.validate(cola("ab"))).isNotEmpty();
    }

    @Test
    void nombreDeColaConCaracteresInvalidosEsRechazado() {
        Set<ConstraintViolation<CrearColaRequest>> errores = validator.validate(cola("mala cola!"));

        assertThat(errores).extracting(ConstraintViolation::getMessage)
                .anyMatch(mensaje -> mensaje.contains("solo permite letras"));
    }

    @Test
    void exchangeConTipoInvalidoEsRechazado() {
        CrearExchangeRequest req = new CrearExchangeRequest();
        req.setName("prueba.exchange");
        req.setType("headers");

        assertThat(validator.validate(req)).isNotEmpty();
    }

    @Test
    void bindingAceptaComodinesEnLaRoutingKey() {
        CrearBindingRequest req = new CrearBindingRequest();
        req.setQueueName("prueba.queue");
        req.setExchangeName("prueba.exchange");
        req.setRoutingKey("orden.creada.#");

        assertThat(validator.validate(req)).isEmpty();
    }
}
