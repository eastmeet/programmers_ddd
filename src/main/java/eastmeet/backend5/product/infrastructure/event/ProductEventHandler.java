package eastmeet.backend5.product.infrastructure.event;

import eastmeet.backend5.product.application.event.ProductCreatedEvent;
import eastmeet.backend5.product.application.event.ProductDeletedEvent;
import eastmeet.backend5.product.application.event.ProductUpdatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class ProductEventHandler {

    @Async("eventExecutor")
    @EventListener
    public void on(ProductCreatedEvent event) {
        log.info("ProductCreatedEvent: productId={}, actorId={}", event.productId(), event.actorId());
    }

    @Async("eventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ProductCreatedEvent event) {
        log.info("ProductCreatedEvent: productId={}, actorId={}", event.productId(), event.actorId());
    }

    @Async("eventExecutor")
    @EventListener
    public void on(ProductUpdatedEvent event) {
        log.info("ProductUpdatedEvent: productId={}, actorId={}", event.productId(), event.actorId());
    }

    @Async("eventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ProductUpdatedEvent event) {
        log.info("ProductUpdatedEvent: productId={}, actorId={}", event.productId(), event.actorId());
    }

    @Async("eventExecutor")
    @EventListener
    public void on(ProductDeletedEvent event) {
        log.info("ProductDeletedEvent: productId={}, actorId={}", event.productId(), event.actorId());
    }

    @Async("eventExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ProductDeletedEvent event) {
        log.info("ProductDeletedEvent: productId={}, actorId={}", event.productId(), event.actorId());
    }
}
