package eastmeet.backend5.product.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

@Schema(description = "[제품 관리] 수정 요청 메시지")
public record UpdateProductRequest(
    @NotBlank
    @Size(max = 100)
    @Schema(description = "제품명", example = "맥북 프로 14인치 (리퍼)")
    String name,

    @Schema(description = "제품 설명", example = "M3 Pro 칩셋, 18GB RAM, 1TB SSD로 업그레이드")
    String description,

    @NotNull
    @DecimalMin(value = "0.0")
    @Schema(description = "제품 가격", example = "2500000.00")
    BigDecimal price,

    @NotNull
    @Min(0)
    @Schema(description = "제품 수량", example = "5")
    Integer stock,

    @NotBlank
    @Size(max = 20)
    @Schema(description = "제품 상태", example = "INACTIVE")
    String status
) {
}