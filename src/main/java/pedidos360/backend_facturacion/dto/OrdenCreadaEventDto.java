package pedidos360.backend_facturacion.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrdenCreadaEventDto {

    @NotNull(message = "El ID de la orden es obligatorio")
    @JsonAlias({"id", "ordenId", "orderId"})
    private Long ordenId;

    @NotBlank(message = "El email del usuario es obligatorio")
    @JsonAlias({"usuarioEmail", "email", "correo"})
    private String usuarioEmail;

    @NotNull(message = "El total de la orden es obligatorio")
    @Positive(message = "El total debe ser mayor a cero")
    @JsonAlias({"total", "montoTotal"})
    private BigDecimal total;
}