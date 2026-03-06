package eastmeet.backend5.product.infrastructure.acl.client;

import java.util.Optional;
import java.util.UUID;

public interface ExternalSellerClient {

    Optional<ExternalSellerPayload> findSeller(UUID sellerId);

}
