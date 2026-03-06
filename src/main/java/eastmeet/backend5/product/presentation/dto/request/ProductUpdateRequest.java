package eastmeet.backend5.product.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "[제품 관리] 수정 요청 메시지")
public record ProductUpdateRequest(
    @Schema(description = "제품명", example = "맥북 프로 14인치 (리퍼)")
    String name,

    @Schema(description = "제품 설명", example = "M3 Pro 칩셋, 18GB RAM, 1TB SSD로 업그레이드")
    String description,

    @Schema(description = "제품 가격", example = "2500000.00")
    BigDecimal price,

    @Schema(description = "제품 수량", example = "5")
    Integer stock,

    @Schema(description = "제품 상태", example = "INACTIVE")
    String status,

    @Schema(description = "변경자 ID", example = "550e8400-e29b-41d4-a716-446655440001")
    String modifierId
) {
}