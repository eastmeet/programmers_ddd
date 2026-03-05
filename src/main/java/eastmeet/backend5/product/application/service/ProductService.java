package eastmeet.backend5.product.application.service;

import eastmeet.backend5.product.application.port.in.ProductUseCase;
import eastmeet.backend5.product.application.port.out.ProductPersistencePort;
import eastmeet.backend5.product.domain.Product;
import eastmeet.backend5.product.dto.in.ProductCreateRequest;
import eastmeet.backend5.product.dto.in.ProductUpdateRequest;
import eastmeet.backend5.product.dto.out.ProductResponse;
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
public class ProductService implements ProductUseCase {

    private final ProductPersistencePort productPersistencePort;

    @Override
    @Transactional
    public ProductResponse create(ProductCreateRequest req) {
        Product product = Product.create(
            toUuid(req.sellerId(), "sellerId"),
            req.name(),
            req.description(),
            req.price(),
            req.stock(),
            req.status(),
            toUuid(req.creatorId(), "creatorId")
        );
        Product savedProduct = productPersistencePort.save(product);
        return ProductResponse.of(savedProduct);
    }

    @Override
    public ProductResponse getById(UUID productId) {
        Product product = findByIdOrThrow(productId);
        return ProductResponse.of(product);
    }

    @Override
    public List<ProductResponse> getAll() {
        List<Product> productList = productPersistencePort.findAll();
        return productList.stream().map(ProductResponse::of).toList();
    }

    @Override
    @Transactional
    public ProductResponse update(UUID productId, ProductUpdateRequest req) {
        Product product = findByIdOrThrow(productId);
        product.update(
            req.name(),
            req.description(),
            req.price(),
            req.stock(),
            req.status(),
            toUuid(req.modifierId(), "modifierId")
        );
        return ProductResponse.of(product);
    }

    @Override
    @Transactional
    public void delete(UUID productId) {
        Product product = findByIdOrThrow(productId);
        productPersistencePort.delete(product);
    }

    private Product findByIdOrThrow(UUID productId) {
        return productPersistencePort.findById(productId)
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
