package eastmeet.backend5.product.infrastructure.acl.client;

import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class StubExternalSellerClient implements ExternalSellerClient {

    @Override
    public Optional<ExternalSellerPayload> findSeller(UUID sellerId) {
        // Local stub for ACL integration; replace with real HTTP/RPC adapter later.
        return Optional.of(new ExternalSellerPayload(sellerId.toString(), "ACTIVE"));
    }
}
