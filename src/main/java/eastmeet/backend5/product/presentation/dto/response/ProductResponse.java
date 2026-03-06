package eastmeet.backend5.product.presentation.dto.response;

import eastmeet.backend5.product.domain.model.Product;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductResponse(
    UUID id,
    UUID sellerId,
    String name,
    String description,
    BigDecimal price,
    Integer stock,
    String status,
    LocalDateTime modifyDt
) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
            product.getId(),
            product.getSellerId(),
            product.getName(),
            product.getDescription(),
            product.getPrice(),
            product.getStock(),
            product.getStatus(),
            product.getModifyDt()
        );
    }
}
