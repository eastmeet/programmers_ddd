package eastmeet.backend5.product.infrastructure.persistence.query;

import eastmeet.backend5.product.domain.model.Product;
import eastmeet.backend5.product.domain.repository.query.ProductQueryRepository;
import eastmeet.backend5.product.infrastructure.persistence.ProductJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductQueryRepositoryAdapter implements ProductQueryRepository {

    private final ProductJpaRepository productJpaRepository;

    @Override
    public Optional<Product> findById(UUID productId) {
        return productJpaRepository.findById(productId);
    }

    @Override
    public List<Product> findAll() {
        return productJpaRepository.findAll();
    }

}
