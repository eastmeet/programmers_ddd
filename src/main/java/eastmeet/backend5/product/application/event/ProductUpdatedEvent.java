package eastmeet.backend5.product.application.event;


import java.util.UUID;

public record ProductUpdatedEvent(UUID productId, UUID actorId) {
}
