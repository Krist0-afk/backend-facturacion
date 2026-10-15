package pedidos360.backend_facturacion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BackendFacturacionApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendFacturacionApplication.class, args);
	}

}
