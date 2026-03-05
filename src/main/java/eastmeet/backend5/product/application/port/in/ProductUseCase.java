package eastmeet.backend5.product.application.port.in;

import eastmeet.backend5.product.domain.Product;
import eastmeet.backend5.product.dto.in.ProductCreateRequest;
import eastmeet.backend5.product.dto.in.ProductUpdateRequest;
import eastmeet.backend5.product.dto.out.ProductResponse;
import java.util.List;
import java.util.UUID;

public interface ProductUseCase {

    ProductResponse create(ProductCreateRequest req);

    ProductResponse getById(UUID productId);

    List<ProductResponse> getAll();

    ProductResponse update(UUID productId, ProductUpdateRequest req);

    void delete(UUID productId);

}
