package eastmeet.backend5.product.application.service;

import eastmeet.backend5.product.application.usecase.ProductUseCase;
import eastmeet.backend5.product.domain.repository.ProductRepository;
import eastmeet.backend5.product.domain.model.Product;
import eastmeet.backend5.product.presentation.dto.request.ProductCreateRequest;
import eastmeet.backend5.product.presentation.dto.request.ProductUpdateRequest;
import eastmeet.backend5.product.presentation.dto.response.ProductResponse;
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
public class ProductApplicationService implements ProductUseCase {

    private final ProductRepository productRepository;

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
        Product savedProduct = productRepository.save(product);
        return ProductResponse.from(savedProduct);
    }

    @Override
    public ProductResponse getById(UUID productId) {
        Product product = findByIdOrThrow(productId);
        return ProductResponse.from(product);
    }

    @Override
    public List<ProductResponse> getAll() {
        List<Product> productList = productRepository.findAll();
        return productList.stream().map(ProductResponse::from).toList();
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
        return ProductResponse.from(product);
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
