package eastmeet.backend5.product.application.usecase;


import eastmeet.backend5.product.domain.model.Product;
import eastmeet.backend5.product.presentation.dto.request.CreateProductRequest;
import eastmeet.backend5.product.presentation.dto.request.UpdateProductRequest;
import java.util.List;
import java.util.UUID;

public interface ProductUseCase {

    Product create(CreateProductRequest request, UUID actorId);

    Product getById(UUID productId);

    List<Product> getAll();

    Product update(UUID productId, UpdateProductRequest request, UUID actorId);

    void delete(UUID productId);
}
