package eastmeet.backend5.product.controller;

import eastmeet.backend5.product.domain.Product;
import eastmeet.backend5.product.dto.ProductCreateRequest;
import eastmeet.backend5.product.dto.ProductUpdateRequest;
import eastmeet.backend5.product.service.ProductService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProductControllerImpl implements ProductController {

    private final ProductService productService;

    public ResponseEntity<Product> create(ProductCreateRequest req) {
        Product response = productService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    public Product getById(UUID productId) {
        return productService.getById(productId);
    }

    public List<Product> getAll() {
        return productService.getAll();
    }

    public Product update(UUID productId, ProductUpdateRequest req) {
        return productService.update(productId, req);
    }

    public ResponseEntity<Void> delete(UUID productId) {
        productService.delete(productId);
        return ResponseEntity.noContent().build();
    }

}
