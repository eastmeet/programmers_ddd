package eastmeet.backend5.product.presentation.dto.response;

import eastmeet.backend5.product.domain.model.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "상품 응답")
public record ProductResponse(
    @Schema(description = "상품 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID id,

    @Schema(description = "판매자 ID", example = "11111111-1111-1111-1111-111111111111")
    UUID sellerId,

    @Schema(description = "상품명", example = "맥북 프로 14")
    String name,

    @Schema(description = "상품 설명", example = "M3 칩셋, 16GB RAM")
    String description,

    @Schema(description = "가격", example = "2590000.00")
    BigDecimal price,

    @Schema(description = "재고", example = "10")
    Integer stock,

    @Schema(description = "상태", example = "ACTIVE")
    String status,

    @Schema(description = "등록자 ID", example = "22222222-2222-2222-2222-222222222222")
    UUID regId,

    @Schema(description = "등록일시", example = "2026-03-04T18:10:00")
    LocalDateTime regDt,

    @Schema(description = "수정자 ID", example = "33333333-3333-3333-3333-333333333333")
    UUID modifyId,

    @Schema(description = "수정일시", example = "2026-03-04T18:12:00")
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
            product.getRegId(),
            product.getRegDt(),
            product.getModifyId(),
            product.getModifyDt()
        );
    }
}
