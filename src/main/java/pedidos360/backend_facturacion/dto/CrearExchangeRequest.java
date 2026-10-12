package pedidos360.backend_facturacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CrearExchangeRequest {

    @NotBlank(message = "El nombre del exchange no puede estar vacío")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Nombre de exchange inválido")
    private String name;

    @NotBlank(message = "El tipo de exchange es obligatorio (direct, topic, fanout)")
    @Pattern(regexp = "^(direct|topic|fanout)$", message = "El tipo de exchange debe ser 'direct', 'topic' o 'fanout'")
    private String type;

    private boolean durable = true;
}