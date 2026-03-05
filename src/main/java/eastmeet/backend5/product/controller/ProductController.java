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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<Product> create(@RequestBody ProductCreateRequest request) {
        Product response = productService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{productId}")
    public Product getById(@PathVariable UUID productId) {
        return productService.getById(productId);
    }

    @GetMapping
    public List<Product> getAll() {
        return productService.getAll();
    }

    @PutMapping("/{productId}")
    public Product update(@PathVariable UUID productId,
        @RequestBody ProductUpdateRequest request) {
        return productService.update(productId, request);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> delete(@PathVariable UUID productId) {
        productService.delete(productId);
        return ResponseEntity.noContent().build();
    }

}
