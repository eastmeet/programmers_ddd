package eastmeet.backend5.product.repository;

import eastmeet.backend5.product.domain.Product;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, UUID> {

}
