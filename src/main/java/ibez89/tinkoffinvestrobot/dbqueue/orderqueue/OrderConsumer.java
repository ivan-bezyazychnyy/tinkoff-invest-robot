package ibez89.tinkoffinvestrobot.dbqueue.orderqueue;

import ibez89.tinkoffinvestrobot.dbqueue.AbstractQueueConsumer;
import ibez89.tinkoffinvestrobot.dbqueue.UuidTaskPayloadTransformer;
import ibez89.tinkoffinvestrobot.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.yoomoney.tech.dbqueue.api.Task;
import ru.yoomoney.tech.dbqueue.api.TaskExecutionResult;
import ru.yoomoney.tech.dbqueue.api.TaskPayloadTransformer;
import ru.yoomoney.tech.dbqueue.settings.QueueConfig;

import javax.annotation.Nonnull;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public class OrderConsumer extends AbstractQueueConsumer<UUID> {

    private static final Logger logger = LoggerFactory.getLogger(OrderConsumer.class);

    private final OrderService orderService;

    public OrderConsumer(QueueConfig queueConfig, OrderService orderService) {
        super(queueConfig);
        this.orderService = orderService;
    }

    @Nonnull
    @Override
    public TaskPayloadTransformer<UUID> getPayloadTransformer() {
        return UuidTaskPayloadTransformer.getInstance();
    }

    @Nonnull
    @Override
    public TaskExecutionResult execute(@Nonnull Task<UUID> task) {
        var processedOrder = orderService.processOrder(task.getPayload().get());
        switch (processedOrder.getStatus()) {
            case NEW:
                var now = Instant.now();
                var placeAt = processedOrder.getPlaceAt();
                if (placeAt != null && placeAt.isAfter(now)) {
                    logger.info("Order is postponed: orderId={}, placeAt={}.",
                            processedOrder.getId(), placeAt);
                    return TaskExecutionResult.reenqueue(Duration.between(now, placeAt));
                } else {
                    return TaskExecutionResult.reenqueue(Duration.ofSeconds(30));
                }
            case PLACED:
                return TaskExecutionResult.reenqueue(Duration.ofSeconds(30));
            case FILLED, REJECTED, UNKNOWN:
                return TaskExecutionResult.finish();
            default:
                throw new IllegalStateException("Unexpected status " + processedOrder.getStatus() +
                        " for order with id " + processedOrder.getId());
        }
    }
}
