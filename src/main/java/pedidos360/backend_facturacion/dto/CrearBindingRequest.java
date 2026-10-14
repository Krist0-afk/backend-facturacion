package pedidos360.backend_facturacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CrearBindingRequest {

    @NotBlank(message = "El nombre de la cola (queueName) es obligatorio")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "queueName solo permite letras, números, puntos, guiones y guiones bajos")
    private String queueName;

    @NotBlank(message = "El nombre del exchange (exchangeName) es obligatorio")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "exchangeName solo permite letras, números, puntos, guiones y guiones bajos")
    private String exchangeName;

    @NotBlank(message = "La routingKey es obligatoria")
    @Size(max = 255, message = "La routingKey no puede superar 255 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9._*#-]+$", message = "La routingKey solo permite letras, números, '.', '-', '_', '*' y '#'")
    private String routingKey;
}
