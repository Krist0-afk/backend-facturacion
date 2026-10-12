package pedidos360.backend_facturacion.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "boletas_sii")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Boleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String folioSii;

    @Column(nullable = false)
    private Long ordenId;

    @Column(nullable = false)
    private String usuarioEmail;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montoNeto;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal iva;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montoTotal;

    @Column(nullable = false)
    private String estado; // EMITIDA, ANULADA

    @Column(nullable = false)
    private LocalDateTime fechaEmision;
}