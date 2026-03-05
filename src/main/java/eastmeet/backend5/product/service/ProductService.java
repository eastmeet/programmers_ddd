package eastmeet.backend5.product.service;

import eastmeet.backend5.product.domain.Product;
import eastmeet.backend5.product.dto.ProductCreateRequest;
import eastmeet.backend5.product.dto.ProductUpdateRequest;
import java.util.List;
import java.util.UUID;

public interface ProductService {

    Product create(ProductCreateRequest req);

    Product getById(UUID productId);

    List<Product> getAll();

    Product update(UUID productId, ProductUpdateRequest req);

    void delete(UUID productId);

}
