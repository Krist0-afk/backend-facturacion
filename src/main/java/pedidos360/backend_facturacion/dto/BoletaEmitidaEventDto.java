package pedidos360.backend_facturacion.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoletaEmitidaEventDto {
    private Long boletaId;
    private String folioSii;
    private Long ordenId;
    private String usuarioEmail;
    private BigDecimal montoNeto;
    private BigDecimal iva;
    private BigDecimal montoTotal;
    private LocalDateTime fechaEmision;
}