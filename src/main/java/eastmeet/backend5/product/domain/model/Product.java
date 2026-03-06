package eastmeet.backend5.product.domain.model;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Getter
@Table(name = "product", schema = "public")
public class Product {

    @Id
    private UUID id;

    @Column(name = "seller_id", nullable = false)
    private UUID sellerId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "reg_id", nullable = false)
    private UUID regId;

    @Column(name = "reg_dt", nullable = false)
    private LocalDateTime regDt;

    @Column(name = "modify_id", nullable = false)
    private UUID modifyId;

    @Column(name = "modify_dt", nullable = false)
    @LastModifiedDate
    private LocalDateTime modifyDt;

    protected Product() {
    }

    private Product(UUID id,
        UUID sellerId,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        String status) {
        this.id = id;
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.status = status;
    }

    public static Product create(UUID sellerId,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        String status,
        UUID creatorId) {
        Product product = new Product(UUID.randomUUID(), sellerId, name, description, price, stock, status);
        product.regId = creatorId;
        product.modifyId = creatorId;
        return product;
    }

    public void update(String name,
        String description,
        BigDecimal price,
        Integer stock,
        String status,
        UUID modifierId) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.status = status;
        this.modifyId = modifierId;
    }

    @PrePersist
    public void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (regId == null) {
            regId = id;
        }
        if (modifyId == null) {
            modifyId = regId;
        }
        if (regDt == null) {
            regDt = LocalDateTime.now();
        }
        if (modifyDt == null) {
            modifyDt = regDt;
        }
        if (status == null) {
            status = "ACTIVE";
        }
        if (stock == null) {
            stock = 0;
        }
    }

    @PreUpdate
    public void onUpdate() {
        modifyDt = LocalDateTime.now();
        if (modifyId == null) {
            modifyId = id;
        }
    }

    public void updateRegDt(LocalDateTime localDateTime) {
        if (localDateTime != null) {
            this.regDt = localDateTime;
        }
    }

    public void updateModifyDt(LocalDateTime localDateTime) {
        if (localDateTime != null) {
            this.modifyDt = localDateTime;
        }
    }

    public void initId(UUID id) {
        if (id != null) {
            this.id = id;
        }
    }

}
