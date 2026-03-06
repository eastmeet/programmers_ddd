package eastmeet.backend5.product.infrastructure.persistence;

import eastmeet.backend5.product.domain.model.Product;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpaRepository extends JpaRepository<Product, UUID> {

}
