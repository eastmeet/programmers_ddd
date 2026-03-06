package eastmeet.backend5.product.domain.repository.query;

import eastmeet.backend5.product.domain.model.Product;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductQueryRepository {

    Optional<Product> findById(UUID productId);

    List<Product> findAll();

}
