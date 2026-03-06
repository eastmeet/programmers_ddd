package eastmeet.backend5.product.application.service;

import eastmeet.backend5.product.application.event.ProductCreatedEvent;
import eastmeet.backend5.product.application.event.ProductDeletedEvent;
import eastmeet.backend5.product.application.event.ProductUpdatedEvent;
import eastmeet.backend5.product.application.exception.ProductNotFoundException;
import eastmeet.backend5.product.application.usecase.ProductUseCase;
import eastmeet.backend5.product.domain.model.Product;
import eastmeet.backend5.product.domain.repository.ProductRepository;
import eastmeet.backend5.product.presentation.dto.request.CreateProductRequest;
import eastmeet.backend5.product.presentation.dto.request.UpdateProductRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly = true)
public class ProductApplicationService implements ProductUseCase {

    private final ProductRepository productRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public ProductApplicationService(ProductRepository productRepository,
        ApplicationEventPublisher applicationEventPublisher) {
        this.productRepository = productRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    @Transactional
    public Product create(CreateProductRequest request, UUID actorId) {
        Product product = Product.create(
            request.sellerId(),
            request.name(),
            request.description(),
            request.price(),
            request.stock(),
            request.status(),
            actorId
        );
        Product saved = productRepository.save(product);
        applicationEventPublisher.publishEvent(new ProductCreatedEvent(saved.getId(), actorId));
        return saved;
    }

    @Override
    public Product getById(UUID productId) {
        return findByIdOrThrow(productId);
    }

    @Override
    public List<Product> getAll() {
        return productRepository.findAll();
    }

    @Override
    @Transactional
    public Product update(UUID productId, UpdateProductRequest request, UUID actorId) {
        Product product = findByIdOrThrow(productId);
        product.update(
            request.name(),
            request.description(),
            request.price(),
            request.stock(),
            request.status(),
            actorId
        );
        applicationEventPublisher.publishEvent(new ProductUpdatedEvent(product.getId(), actorId));
        return product;
    }

    @Override
    @Transactional
    public void delete(UUID productId) {
        Product product = findByIdOrThrow(productId);
        productRepository.delete(product);
        applicationEventPublisher.publishEvent(new ProductDeletedEvent(productId, product.getModifyId()));
    }

    private Product findByIdOrThrow(UUID productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ProductNotFoundException(productId));
    }
}
