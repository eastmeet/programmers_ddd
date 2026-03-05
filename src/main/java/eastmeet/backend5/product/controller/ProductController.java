package eastmeet.backend5.product.controller;

import eastmeet.backend5.product.domain.Product;
import eastmeet.backend5.product.dto.ProductCreateRequest;
import eastmeet.backend5.product.dto.ProductUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "[제품 관리] 생성")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "생성 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping
    ResponseEntity<Product> create(@RequestBody ProductCreateRequest request);

    @Operation(summary = "[제품 관리] 단건 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "404", description = "제품 없음")
    })
    @GetMapping("/{productId}")
    Product getById(@PathVariable UUID productId);

    @Operation(summary = "[제품 관리] 전체 조회")
    @ApiResponses(
        @ApiResponse(responseCode = "200", description = "조회 성공")
    )
    @GetMapping
    List<Product> getAll();

    @Operation(summary = "[제품 관리] 단건 수정")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청"),
        @ApiResponse(responseCode = "404", description = "제품 없음")
    })
    @PutMapping("/{productId}")
    Product update(@PathVariable UUID productId, @RequestBody ProductUpdateRequest request);

    @Operation(summary = "[제품 관리] 단건 삭제")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "삭제 성공"),
        @ApiResponse(responseCode = "404", description = "제품 없음")
    })
    @DeleteMapping("/{productId}")
    ResponseEntity<Void> delete(@PathVariable UUID productId);

}
