package ibez89.tinkoffinvestrobot.dbqueue;

import lombok.RequiredArgsConstructor;
import ru.yoomoney.tech.dbqueue.api.QueueConsumer;
import ru.yoomoney.tech.dbqueue.settings.QueueConfig;

import javax.annotation.Nonnull;

@RequiredArgsConstructor
public abstract class AbstractQueueConsumer<T> implements QueueConsumer<T> {

    private final QueueConfig queueConfig;

    @Nonnull
    @Override
    public QueueConfig getQueueConfig() {
        return queueConfig;
    }
}
