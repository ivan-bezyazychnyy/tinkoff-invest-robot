package ibez89.tinkoffinvestrobot.dbqueue;

import lombok.RequiredArgsConstructor;
import ru.yoomoney.tech.dbqueue.api.QueueConsumer;
import ru.yoomoney.tech.dbqueue.config.QueueService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;

@RequiredArgsConstructor
public class SpringQueueService {

    private final QueueService queueService;

    private final List<QueueConsumer<?>> queueConsumers;

    @PostConstruct
    public void postConstruct() {
        queueConsumers.forEach(queueService::registerQueue);
        queueService.start();
    }

    @PreDestroy
    public void preDestroy() {
        queueService.shutdown();
    }
}
