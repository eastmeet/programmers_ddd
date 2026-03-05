package eastmeet.backend5.product.adapter.out.persistence;

import eastmeet.backend5.product.domain.Product;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductJpaRepository extends JpaRepository<Product, UUID> {

}
