package eastmeet.backend5.product.service;

import eastmeet.backend5.product.domain.Product;
import eastmeet.backend5.product.dto.ProductCreateRequest;
import eastmeet.backend5.product.dto.ProductUpdateRequest;
import eastmeet.backend5.product.repository.ProductRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public Product create(ProductCreateRequest req) {
        Product product = Product.create(
            toUuid(req.sellerId(), "sellerId"),
            req.name(),
            req.description(),
            req.price(),
            req.stock(),
            req.status(),
            toUuid(req.creatorId(), "creatorId")
        );
        return productRepository.save(product);
    }

    @Override
    public Product getById(UUID productId) {
        Product product = findByIdOrThrow(productId);
        return product;
    }

    @Override
    public List<Product> getAll() {
        return productRepository.findAll();
    }

    @Override
    @Transactional
    public Product update(UUID productId, ProductUpdateRequest req) {
        Product product = findByIdOrThrow(productId);
        product.update(
            req.name(),
            req.description(),
            req.price(),
            req.stock(),
            req.status(),
            toUuid(req.modifierId(), "modifierId")
        );
        return product;
    }

    @Override
    @Transactional
    public void delete(UUID productId) {
        Product product = findByIdOrThrow(productId);
        productRepository.delete(product);
    }

    private Product findByIdOrThrow(UUID productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }

    private UUID toUuid(String value, String fieldName) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            log.error("{} must be valid UUID: {}", fieldName, value, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " must be valid UUID");
        }
    }
}
