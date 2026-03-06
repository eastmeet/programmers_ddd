package eastmeet.backend5.product.infrastructure.persistence.command;

import eastmeet.backend5.product.domain.model.Product;
import eastmeet.backend5.product.domain.repository.command.ProductCommandRepository;
import eastmeet.backend5.product.infrastructure.persistence.ProductJpaRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ProductCommandRepositoryAdapter implements ProductCommandRepository {

    private final ProductJpaRepository productJpaRepository;

    @Override
    public Product save(Product product) {
        return productJpaRepository.save(product);
    }

    @Override
    public Optional<Product> findById(UUID productId) {
        return productJpaRepository.findById(productId);
    }

    @Override
    public void delete(Product product) {
        productJpaRepository.delete(product);
    }

}
