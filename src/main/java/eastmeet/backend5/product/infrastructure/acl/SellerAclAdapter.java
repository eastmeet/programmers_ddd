package eastmeet.backend5.product.infrastructure.acl;

import eastmeet.backend5.product.application.acl.SellerAcl;
import eastmeet.backend5.product.application.acl.SellerIdentity;
import eastmeet.backend5.product.application.exception.InactiveSellerException;
import eastmeet.backend5.product.application.exception.SellerNotFoundException;
import eastmeet.backend5.product.infrastructure.acl.client.ExternalSellerClient;
import eastmeet.backend5.product.infrastructure.acl.client.ExternalSellerPayload;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class SellerAclAdapter implements SellerAcl {

    private static final String ACTIVE_STATUS = "ACTIVE";

    private final ExternalSellerClient externalSellerClient;

    public SellerAclAdapter(ExternalSellerClient externalSellerClient) {
        this.externalSellerClient = externalSellerClient;
    }

    @Override
    public SellerIdentity loadActiveSeller(UUID sellerId) {
        ExternalSellerPayload payload = externalSellerClient.findSeller(sellerId)
            .orElseThrow(() -> new SellerNotFoundException(sellerId));

        if (!ACTIVE_STATUS.equalsIgnoreCase(payload.sellerStatusCode())) {
            throw new InactiveSellerException(sellerId);
        }

        return new SellerIdentity(parseSellerId(payload.sellerNo()));
    }

    private UUID parseSellerId(String sellerNo) {
        try {
            return UUID.fromString(sellerNo);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Invalid seller id from external system. sellerNo=" + sellerNo,
                ex);
        }
    }
}
