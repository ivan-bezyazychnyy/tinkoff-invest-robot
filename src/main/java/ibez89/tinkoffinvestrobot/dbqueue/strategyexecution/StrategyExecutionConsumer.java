package ibez89.tinkoffinvestrobot.dbqueue.strategyexecution;

import ibez89.tinkoffinvestrobot.dbqueue.AbstractQueueConsumer;
import ibez89.tinkoffinvestrobot.dbqueue.UuidTaskPayloadTransformer;
import ibez89.tinkoffinvestrobot.service.StrategyService;
import ru.yoomoney.tech.dbqueue.api.Task;
import ru.yoomoney.tech.dbqueue.api.TaskExecutionResult;
import ru.yoomoney.tech.dbqueue.api.TaskPayloadTransformer;
import ru.yoomoney.tech.dbqueue.settings.QueueConfig;

import java.time.Duration;
import java.util.UUID;

public class StrategyExecutionConsumer extends AbstractQueueConsumer<UUID> {

    private final StrategyService strategyService;

    public StrategyExecutionConsumer(QueueConfig queueConfig, StrategyService strategyService) {
       super(queueConfig);
        this.strategyService = strategyService;
    }

    @Override
    public TaskPayloadTransformer<UUID> getPayloadTransformer() {
        return UuidTaskPayloadTransformer.getInstance();
    }

    @Override
    public TaskExecutionResult execute(Task<UUID> task) {
        return switch (strategyService.performStrategyExecution(task.getPayload().get())) {
            case RESCHEDULE -> TaskExecutionResult.reenqueue(Duration.ofMinutes(1));
            case COMPLETE -> TaskExecutionResult.finish();
        };
    }


}
