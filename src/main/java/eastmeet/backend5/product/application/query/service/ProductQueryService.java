package eastmeet.backend5.product.application.query.service;

import eastmeet.backend5.product.application.query.usecase.ProductQueryUseCase;
import eastmeet.backend5.product.domain.model.Product;
import eastmeet.backend5.product.domain.repository.query.ProductQueryRepository;
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
public class ProductQueryService implements ProductQueryUseCase {

    private final ProductQueryRepository productQueryRepository;

    @Override
    public ProductResponse getById(UUID productId) {
        Product product = findByIdOrThrow(productId);
        return ProductResponse.from(product);
    }

    @Override
    public List<ProductResponse> getAll() {
        List<Product> productList = productQueryRepository.findAll();
        return productList.stream().map(ProductResponse::from).toList();
    }

    private Product findByIdOrThrow(UUID productId) {
        return productQueryRepository.findById(productId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
    }
}
