package eastmeet.backend5.product.application.event;


import java.util.UUID;

public record ProductDeletedEvent(UUID productId, UUID actorId) {
}
