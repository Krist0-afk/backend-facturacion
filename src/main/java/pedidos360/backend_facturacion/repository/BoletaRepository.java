package pedidos360.backend_facturacion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pedidos360.backend_facturacion.entity.Boleta;

import java.util.List;
import java.util.Optional;

public interface BoletaRepository extends JpaRepository<Boleta, Long> {
    List<Boleta> findByUsuarioEmailOrderByFechaEmisionDesc(String usuarioEmail);
    Optional<Boleta> findByOrdenId(Long ordenId);
}