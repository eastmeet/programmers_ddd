package eastmeet.backend5.product.dto.in;


import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "[제품 관리] 등록 요청 메시지")
public record ProductCreateRequest(
    @Schema(description = "판매자 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    String sellerId,

    @Schema(description = "제품명", example = "맥북 프로 14인치")
    String name,

    @Schema(description = "제품 설명", example = "M3 Pro 칩셋, 18GB RAM, 512GB SSD")
    String description,

    @Schema(description = "제품 가격", example = "2990000.00")
    BigDecimal price,

    @Schema(description = "제품 수량", example = "10")
    Integer stock,

    @Schema(description = "제품 상태", example = "ACTIVE")
    String status,

    @Schema(description = "작성자 ID", example = "550e8400-e29b-41d4-a716-446655440001")
    String creatorId
) {
}