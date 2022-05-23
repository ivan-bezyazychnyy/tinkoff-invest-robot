package ibez89.tinkoffinvestrobot.service.strategies;

import ibez89.tinkoffinvestrobot.api.model.StrategyType;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class StrategyExecutionProcessorManager {

    private final Map<StrategyType, StrategyExecutionProcessor> strategyExecutionProccesorByStrategyTypeMap;

    public StrategyExecutionProcessorManager(List<StrategyExecutionProcessor> strategyExecutionProcessors) {
        this.strategyExecutionProccesorByStrategyTypeMap = strategyExecutionProcessors.stream()
                .collect(Collectors.toMap(StrategyExecutionProcessor::getStrategyType, Function.identity()));
    }

    public StrategyExecutionProcessor getStrategyExecutionProcessor(StrategyType strategyType) {
        if (!strategyExecutionProccesorByStrategyTypeMap.containsKey(strategyType)) {
            throw new IllegalStateException("Strategy type is not supported: " + strategyType);
        }
        return strategyExecutionProccesorByStrategyTypeMap.get(strategyType);
    }
}
