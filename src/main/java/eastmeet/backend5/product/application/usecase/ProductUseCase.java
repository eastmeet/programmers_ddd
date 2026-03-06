package eastmeet.backend5.product.application.usecase;

import eastmeet.backend5.product.presentation.dto.request.ProductCreateRequest;
import eastmeet.backend5.product.presentation.dto.request.ProductUpdateRequest;
import eastmeet.backend5.product.presentation.dto.response.ProductResponse;
import java.util.List;
import java.util.UUID;

public interface ProductUseCase {

    ProductResponse create(ProductCreateRequest req);

    ProductResponse getById(UUID productId);

    List<ProductResponse> getAll();

    ProductResponse update(UUID productId, ProductUpdateRequest req);

    void delete(UUID productId);

}
