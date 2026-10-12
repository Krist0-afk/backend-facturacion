package pedidos360.backend_facturacion.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CrearColaRequest {

    @NotBlank(message = "El nombre de la cola no puede estar vacío")
    @Size(min = 3, max = 100, message = "El nombre de la cola debe tener entre 3 y 100 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "El nombre de la cola solo permite letras, números, puntos, guiones y guiones bajos")
    private String name;

    private boolean durable = true;
    private boolean exclusive = false;
    private boolean autoDelete = false;
    private String deadLetterExchange;
    private String deadLetterRoutingKey;
}