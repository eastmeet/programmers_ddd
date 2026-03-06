package eastmeet.backend5.product.application.query.usecase;

import eastmeet.backend5.product.presentation.dto.response.ProductResponse;
import java.util.List;
import java.util.UUID;

public interface ProductQueryUseCase {

    ProductResponse getById(UUID productId);

    List<ProductResponse> getAll();

}
