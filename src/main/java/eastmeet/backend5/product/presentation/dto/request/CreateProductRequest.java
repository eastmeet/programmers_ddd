package eastmeet.backend5.product.presentation.dto.request;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "[제품 관리] 등록 요청 메시지")
public record CreateProductRequest(
    @NotNull
    @Schema(description = "판매자 ID", example = "550e8400-e29b-41d4-a716-446655440000")
    UUID sellerId,

    @NotBlank
    @Size(max = 100)
    @Schema(description = "제품명", example = "맥북 프로 14인치")
    String name,

    @Schema(description = "제품 설명", example = "M3 Pro 칩셋, 18GB RAM, 512GB SSD")
    String description,

    @NotNull
    @DecimalMin(value = "0.0")
    @Schema(description = "제품 가격", example = "2990000.00")
    BigDecimal price,

    @NotNull
    @Min(0)
    @Schema(description = "제품 수량", example = "10")
    Integer stock,

    @NotBlank
    @Size(max = 20)
    @Schema(description = "제품 상태", example = "ACTIVE")
    String status
) {
}