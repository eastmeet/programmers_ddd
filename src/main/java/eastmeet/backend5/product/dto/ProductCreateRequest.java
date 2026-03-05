package eastmeet.backend5.product.dto;


import java.math.BigDecimal;

public record ProductCreateRequest(
    String sellerId,
    String name,
    String description,
    BigDecimal price,
    Integer stock,
    String status,
    String creatorId
) {
}