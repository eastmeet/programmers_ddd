package eastmeet.backend5.product.presentation.controller;

import eastmeet.backend5.product.presentation.dto.request.ProductCreateRequest;
import eastmeet.backend5.product.presentation.dto.request.ProductUpdateRequest;
import eastmeet.backend5.product.application.usecase.ProductUseCase;
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

    public ResponseEntity<ProductResponse> create(ProductCreateRequest req) {
        ProductResponse response = productUseCase.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    public ProductResponse getById(UUID productId) {
        return productUseCase.getById(productId);
    }

    public List<ProductResponse> getAll() {
        return productUseCase.getAll();
    }

    public ProductResponse update(UUID productId, ProductUpdateRequest req) {
        return productUseCase.update(productId, req);
    }

    public ResponseEntity<Void> delete(UUID productId) {
        productUseCase.delete(productId);
        return ResponseEntity.noContent().build();
    }

}
