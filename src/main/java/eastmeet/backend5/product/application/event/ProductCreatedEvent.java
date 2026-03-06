package eastmeet.backend5.product.application.event;


import java.util.UUID;

public record ProductCreatedEvent(UUID productId, UUID actorId) {
}
