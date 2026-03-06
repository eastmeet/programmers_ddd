package eastmeet.backend5.product.presentation.controller;

import eastmeet.backend5.product.application.usecase.ProductUseCase;
import eastmeet.backend5.product.domain.model.Product;
import eastmeet.backend5.product.presentation.dto.request.CreateProductRequest;
import eastmeet.backend5.product.presentation.dto.request.UpdateProductRequest;
import eastmeet.backend5.product.presentation.dto.response.ProductResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProductControllerImpl implements ProductController {

    private final ProductUseCase productUseCase;

    public ResponseEntity<ProductResponse> create(CreateProductRequest req, UUID actorId) {
        Product product = productUseCase.create(req, actorId);
        ProductResponse response = ProductResponse.from(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    public ProductResponse getById(UUID productId) {
        return ProductResponse.from(productUseCase.getById(productId));
    }

    public List<ProductResponse> getAll() {
        return productUseCase.getAll().stream().map(ProductResponse::from).toList();
    }

    public ProductResponse update(UUID productId, UpdateProductRequest req, UUID actorId) {
        Product updated = productUseCase.update(productId, req, actorId);
        return ProductResponse.from(updated);
    }

    public ResponseEntity<Void> delete(UUID productId) {
        productUseCase.delete(productId);
        return ResponseEntity.noContent().build();
    }

}
