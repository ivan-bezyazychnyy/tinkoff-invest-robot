package ibez89.tinkoffinvestrobot.configuration;

import ibez89.tinkoffinvestrobot.dbqueue.UuidTaskPayloadTransformer;
import ibez89.tinkoffinvestrobot.dbqueue.orderqueue.OrderConsumer;
import ibez89.tinkoffinvestrobot.service.OrderService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.yoomoney.tech.dbqueue.api.QueueProducer;
import ru.yoomoney.tech.dbqueue.api.impl.ShardingQueueProducer;
import ru.yoomoney.tech.dbqueue.api.impl.SingleQueueShardRouter;
import ru.yoomoney.tech.dbqueue.config.QueueShard;
import ru.yoomoney.tech.dbqueue.settings.*;

import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.UUID;

@Configuration
public class OrderQueueConfiguration {

    @Bean
    QueueConfig orderQueueConfig() {
        var queueSettings = QueueSettings.builder()
                .withProcessingSettings(
                        ProcessingSettings.builder()
                                .withProcessingMode(ProcessingMode.SEPARATE_TRANSACTIONS)
                                .withThreadCount(1)
                                .build())
                .withPollSettings(
                        PollSettings.builder()
                                .withBetweenTaskTimeout(Duration.ofMillis(500))
                                .withNoTaskTimeout(Duration.ofSeconds(1))
                                .withFatalCrashTimeout(Duration.ofSeconds(30))
                                .build())
                .withFailureSettings(
                        FailureSettings.builder()
                                .withRetryType(FailRetryType.GEOMETRIC_BACKOFF)
                                .withRetryInterval(Duration.ofSeconds(30))
                                .build())
                .withReenqueueSettings(
                        ReenqueueSettings.builder()
                                .withRetryType(ReenqueueRetryType.MANUAL)
                                .build())
                .withExtSettings(
                        ExtSettings.builder()
                                .withSettings(new LinkedHashMap<>())
                                .build())
                .build();
        return new QueueConfig(
                QueueLocation.builder()
                        .withTableName("queue_task")
                        .withQueueId(new QueueId("order-queue"))
                        .build(),
                queueSettings);
    }

    @Bean
    public QueueProducer<UUID> orderTaskProducer(
            QueueConfig orderQueueConfig,
            QueueShard<?> queueShard) {
        return new ShardingQueueProducer<>(
                orderQueueConfig,
                UuidTaskPayloadTransformer.getInstance(),
                new SingleQueueShardRouter<>(queueShard));
    }

    @Bean
    public OrderConsumer orderConsumer(
            QueueConfig orderQueueConfig,
            OrderService orderService) {
        return new OrderConsumer(orderQueueConfig, orderService);
    }
}
