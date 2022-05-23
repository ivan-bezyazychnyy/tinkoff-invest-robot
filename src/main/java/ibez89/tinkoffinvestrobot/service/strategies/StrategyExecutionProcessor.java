package ibez89.tinkoffinvestrobot.service.strategies;

import ibez89.tinkoffinvestrobot.api.model.StrategyType;
import ibez89.tinkoffinvestrobot.model.Strategy;
import ibez89.tinkoffinvestrobot.model.StrategyExecution;
import ibez89.tinkoffinvestrobot.service.StrategyExecutionProcessingResult;

public interface StrategyExecutionProcessor {

    StrategyType getStrategyType();

    StrategyExecutionProcessingResult process(Strategy strategy, StrategyExecution strategyExecution);
}
