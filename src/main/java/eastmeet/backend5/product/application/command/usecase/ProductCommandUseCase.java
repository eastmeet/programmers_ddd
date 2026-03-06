package eastmeet.backend5.product.application.command.usecase;

import eastmeet.backend5.product.presentation.dto.request.ProductCreateRequest;
import eastmeet.backend5.product.presentation.dto.request.ProductUpdateRequest;
import eastmeet.backend5.product.presentation.dto.response.ProductResponse;
import java.util.UUID;

public interface ProductCommandUseCase {

    ProductResponse create(ProductCreateRequest req);

    ProductResponse update(UUID productId, ProductUpdateRequest req);

    void delete(UUID productId);

}
