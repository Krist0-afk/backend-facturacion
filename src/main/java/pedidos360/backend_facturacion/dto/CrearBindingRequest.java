package pedidos360.backend_facturacion.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CrearBindingRequest {

    @NotBlank(message = "El nombre de la cola (queueName) es obligatorio")
    private String queueName;

    @NotBlank(message = "El nombre del exchange (exchangeName) es obligatorio")
    private String exchangeName;

    @NotBlank(message = "La routingKey es obligatoria")
    private String routingKey;
}