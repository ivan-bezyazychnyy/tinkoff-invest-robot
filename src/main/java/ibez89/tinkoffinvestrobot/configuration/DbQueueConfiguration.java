package ibez89.tinkoffinvestrobot.configuration;

import ibez89.tinkoffinvestrobot.dbqueue.SpringQueueService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import ru.yoomoney.tech.dbqueue.api.QueueConsumer;
import ru.yoomoney.tech.dbqueue.config.*;
import ru.yoomoney.tech.dbqueue.config.impl.LoggingTaskLifecycleListener;
import ru.yoomoney.tech.dbqueue.config.impl.LoggingThreadLifecycleListener;
import ru.yoomoney.tech.dbqueue.spring.dao.SpringDatabaseAccessLayer;

import java.util.List;

import static java.util.Collections.singletonList;

@Configuration
public class DbQueueConfiguration {

    @Bean
    public QueueShard<SpringDatabaseAccessLayer> queueShard(
            JdbcTemplate jdbcTemplate,
            TransactionTemplate transactionTemplate) {
        var databaseAccessLayer = new SpringDatabaseAccessLayer(
                DatabaseDialect.POSTGRESQL,
                QueueTableSchema.builder().build(),
                jdbcTemplate,
                transactionTemplate);
        return new QueueShard<>(new QueueShardId("main"), databaseAccessLayer);
    }

    @Bean
    public QueueService queueService(QueueShard<?> queueShard) {
        return new QueueService(
                singletonList(queueShard),
                new LoggingThreadLifecycleListener(),
                new LoggingTaskLifecycleListener());
    }

    @Bean
    public SpringQueueService springQueueService(
            QueueService queueService,
            List<QueueConsumer<?>> queueConsumers) {
        return new SpringQueueService(queueService, queueConsumers);
    }
}
