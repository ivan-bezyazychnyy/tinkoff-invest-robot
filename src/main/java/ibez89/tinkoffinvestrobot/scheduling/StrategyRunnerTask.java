package ibez89.tinkoffinvestrobot.scheduling;

import ibez89.tinkoffinvestrobot.service.StrategyService;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class StrategyRunnerTask {

    private final StrategyService strategyService;

    private static final Logger logger = LoggerFactory.getLogger(StrategyRunnerTask.class);

    @Scheduled(fixedDelayString = "${application.scheduling.strategy-runner-task.fixed-delay}")
    public void run() {
        var startTime = System.currentTimeMillis();
        strategyService.executePendingStrategies();
        logger.info("{} executed in {} millis.", getClass().getSimpleName(), System.currentTimeMillis() - startTime);
    }
}
