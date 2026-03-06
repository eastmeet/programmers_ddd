package eastmeet.backend5.product.presentation.controller;

import eastmeet.backend5.product.application.command.usecase.ProductCommandUseCase;
import eastmeet.backend5.product.application.query.usecase.ProductQueryUseCase;
import eastmeet.backend5.product.presentation.dto.request.ProductCreateRequest;
import eastmeet.backend5.product.presentation.dto.request.ProductUpdateRequest;
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

    private final ProductCommandUseCase productCommandUseCase;
    private final ProductQueryUseCase productQueryUseCase;

    public ResponseEntity<ProductResponse> create(ProductCreateRequest req) {
        ProductResponse response = productCommandUseCase.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    public ProductResponse getById(UUID productId) {
        return productQueryUseCase.getById(productId);
    }

    public List<ProductResponse> getAll() {
        return productQueryUseCase.getAll();
    }

    public ProductResponse update(UUID productId, ProductUpdateRequest req) {
        return productCommandUseCase.update(productId, req);
    }

    public ResponseEntity<Void> delete(UUID productId) {
        productCommandUseCase.delete(productId);
        return ResponseEntity.noContent().build();
    }

}
