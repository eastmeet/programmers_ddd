package eastmeet.backend5.sevice;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eastmeet.backend5.product.application.acl.SellerAcl;
import eastmeet.backend5.product.application.acl.SellerIdentity;
import eastmeet.backend5.product.application.exception.ProductNotFoundException;
import eastmeet.backend5.product.application.exception.SellerNotFoundException;
import eastmeet.backend5.product.application.service.ProductApplicationService;
import eastmeet.backend5.product.domain.model.Product;
import eastmeet.backend5.product.domain.repository.ProductRepository;
import eastmeet.backend5.product.presentation.dto.request.CreateProductRequest;
import eastmeet.backend5.product.presentation.dto.request.UpdateProductRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductApplicationServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SellerAcl sellerAcl;

    @InjectMocks
    private ProductApplicationService productApplicationService;

    @Test
    void createSetsActorIdToRegIdAndModifyId() {
        UUID actorId = UUID.randomUUID();

        CreateProductRequest request = new CreateProductRequest(
            UUID.randomUUID(),
            "Macbook Pro 14",
            "M3 chip",
            new BigDecimal("2590000.00"),
            10,
            "ACTIVE"
        );

        when(sellerAcl.loadActiveSeller(request.sellerId())).thenReturn(new SellerIdentity(request.sellerId()));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Product created = productApplicationService.create(request, actorId);

        assertThat(created.getRegId()).isEqualTo(actorId);
        assertThat(created.getModifyId()).isEqualTo(actorId);
        verify(sellerAcl).loadActiveSeller(request.sellerId());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createThrowsWhenSellerDoesNotExist() {
        UUID actorId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();

        CreateProductRequest request = CreateProductRequest.builder()
            .sellerId(sellerId)
            .name("Macbook Pro 14")
            .description("M3 chip")
            .price(new BigDecimal("2590000.00"))
            .stock(10)
            .status("ACTIVE")
            .build();

        when(sellerAcl.loadActiveSeller(sellerId)).thenThrow(new SellerNotFoundException(sellerId));

        assertThatThrownBy(() -> productApplicationService.create(request, actorId))
            .isInstanceOf(SellerNotFoundException.class)
            .hasMessageContaining(sellerId.toString());
    }

    @Test
    void getAllReturnsProducts() {
        Product p1 = Product.create(
            UUID.randomUUID(),
            "Product1",
            null,
            new BigDecimal("100.00"),
            1,
            "ACTIVE",
            UUID.randomUUID()
        );
        Product p2 = Product.create(
            UUID.randomUUID(),
            "Product2",
            null,
            new BigDecimal("200.00"),
            2,
            "ACTIVE",
            UUID.randomUUID()
        );

        when(productRepository.findAll()).thenReturn(List.of(p1, p2));

        List<Product> result = productApplicationService.getAll();

        assertThat(result).hasSize(2);
        verify(productRepository).findAll();
    }

    @Test
    void updateChangesProductAndModifierId() {
        UUID productId = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();

        Product existing = Product.create(
            UUID.randomUUID(),
            "Old Product",
            "Old",
            new BigDecimal("100.00"),
            1,
            "ACTIVE",
            UUID.randomUUID()
        );

        existing.initId(productId);

        UpdateProductRequest request = new UpdateProductRequest(
            "New Product",
            "New",
            new BigDecimal("300.00"),
            5,
            "ACTIVE"
        );

        when(productRepository.findById(productId)).thenReturn(Optional.of(existing));

        Product updated = productApplicationService.update(productId, request, actorId);

        assertThat(updated.getName()).isEqualTo("New Product");
        assertThat(updated.getDescription()).isEqualTo("New");
        assertThat(updated.getPrice()).isEqualByComparingTo("300.00");
        assertThat(updated.getStock()).isEqualTo(5);
        assertThat(updated.getModifyId()).isEqualTo(actorId);
    }

    @Test
    void deleteRemovesProductWhenExists() {
        UUID productId = UUID.randomUUID();
        Product existing = Product.create(
            UUID.randomUUID(),
            "Product",
            null,
            new BigDecimal("100.00"),
            1,
            "ACTIVE",
            UUID.randomUUID()
        );
        existing.initId(productId);

        when(productRepository.findById(productId)).thenReturn(Optional.of(existing));

        productApplicationService.delete(productId);

        verify(productRepository).delete(existing);
    }

    @Test
    void getByIdThrowsWhenProductDoesNotExist() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productApplicationService.getById(productId))
            .isInstanceOf(ProductNotFoundException.class)
            .hasMessageContaining(productId.toString());
    }
}
