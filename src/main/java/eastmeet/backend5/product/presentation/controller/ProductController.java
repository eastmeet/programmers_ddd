package eastmeet.backend5.product.presentation.controller;

import eastmeet.backend5.product.presentation.dto.request.ProductCreateRequest;
import eastmeet.backend5.product.presentation.dto.request.ProductUpdateRequest;
import eastmeet.backend5.product.presentation.dto.response.ProductResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "[DDD] PRODUCT", description = "제품 관리 API 입니다.")
@Validated
@RequestMapping("/api/v1/products")
public interface ProductController {

    @PostMapping
    @Operation(summary = "[제품 관리] 생성", description = "신규 제품을 생성합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    ResponseEntity<ProductResponse> create(@RequestBody ProductCreateRequest request);

    @GetMapping("/{productId}")
    @Operation(summary = "[제품 관리] 단건 조회", description = "제품 ID로 상품 정보를 조회합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "제품 없음")
    })
    ProductResponse getById(@Parameter(description = "상품 UUID") @PathVariable UUID productId);

    @GetMapping
    @Operation(summary = "[제품 관리] 전체 조회", description = "전체 상품 목록을 조회합니다.")
    @ApiResponses(
        @ApiResponse(responseCode = "200", description = "조회 성공")
    )
    List<ProductResponse> getAll();

    @PutMapping("/{productId}")
    @Operation(summary = "[제품 관리] 단건 수정", description = "제품 정보를 수정합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "404", description = "제품 없음")
    })
    ProductResponse update(@PathVariable UUID productId, @RequestBody ProductUpdateRequest request);

    @DeleteMapping("/{productId}")
    @Operation(summary = "[제품 관리] 단건 삭제", description = "제품을 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
        @ApiResponse(responseCode = "404", description = "제품 없음")
    })
    ResponseEntity<Void> delete(@PathVariable UUID productId);

}
