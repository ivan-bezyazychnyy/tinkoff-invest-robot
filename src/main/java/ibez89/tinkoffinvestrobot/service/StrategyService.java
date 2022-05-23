package ibez89.tinkoffinvestrobot.service;

import ibez89.tinkoffinvestrobot.api.dto.CreateStrategyDto;
import ibez89.tinkoffinvestrobot.api.dto.UpdateStrategyDto;
import ibez89.tinkoffinvestrobot.api.model.StrategyStatus;
import ibez89.tinkoffinvestrobot.model.Strategy;
import ibez89.tinkoffinvestrobot.model.StrategyExecution;
import ibez89.tinkoffinvestrobot.repository.StrategyRepository;
import ibez89.tinkoffinvestrobot.service.strategies.StrategyExecutionProcessorManager;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StrategyService {

    private static final Logger logger = LoggerFactory.getLogger(StrategyService.class);

    private final StrategyRepository strategyRepository;

    private final StrategyExecutionService strategyExecutionService;

    private final StrategyExecutionProcessorManager strategyExecutionProcessorManager;

    public Page<Strategy> getStrategies(Pageable pageable) {
        return strategyRepository.findAll(pageable);
    }

    public Strategy getStrategy(UUID id) {
        return strategyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Strategy not found"));
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public Strategy archiveStrategy(UUID id) {
        var strategy = strategyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Strategy not found"));
        if (strategy.getStatus() == StrategyStatus.ARCHIVED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Strategy is already archived");
        }
        strategy.setStatus(StrategyStatus.ARCHIVED);
        logger.info("Archiving strategy: id={}.", strategy.getId());
        return strategy;
    }

    public Strategy createStrategy(CreateStrategyDto createStrategyDto) {
        return strategyRepository.save(
                Strategy.builder()
                        .tinkoffAccountId(createStrategyDto.getTinkoffAccountId())
                        .sandbox(createStrategyDto.isSandbox())
                        .type(createStrategyDto.getType())
                        .executionPeriod(createStrategyDto.getExecutionPeriod())
                        .instruments(createStrategyDto.getInstruments().stream()
                                .distinct()
                                .collect(Collectors.toList()))
                        .status(StrategyStatus.ACTIVE)
                        .build());
    }

    @Transactional
    public Strategy updateStrategy(UUID strategyId, UpdateStrategyDto updateStrategyDto) {
        var strategy = getStrategy(strategyId);
        strategy.setExecutionPeriod(updateStrategyDto.getExecutionPeriod());
        strategy.setInstruments(updateStrategyDto.getInstruments());
        strategyRepository.save(strategy);
        return strategy;
    }

    public void executePendingStrategies() {
        var blockedAccountIds = new HashSet<String>();
        for (var strategy : strategyRepository.findPendingActiveStrategiesWithoutAccountExecutions()) {
            if (blockedAccountIds.contains(strategy.getTinkoffAccountId())) {
                logger.info("Skipping strategy due to active execution on the same account: " +
                        " strategyId={}, tinkoffAccountId={}.",
                        strategy.getId(), strategy.getTinkoffAccountId());
                continue;
            }
            var createdExecution = createExecution(strategy);
            blockedAccountIds.add(createdExecution.getTinkoffAccountId());
        }
    }

    private StrategyExecution createExecution(Strategy strategy) {
        var execution = strategyExecutionService.createStrategyExecution(
                StrategyExecution.builder()
                        .strategyId(strategy.getId())
                        .tinkoffAccountId(strategy.getTinkoffAccountId())
                        .sandbox(strategy.isSandbox())
                        .startedAt(Instant.now())
                        .build());
        logger.info("Created execution for strategy: strategyId={}, executionId={}.",
                strategy.getId(), execution.getId());
        return execution;
    }

    public StrategyExecutionProcessingResult performStrategyExecution(UUID strategyExecutionId) {
        var strategyExecution = strategyExecutionService.getStrategyExecutionById(strategyExecutionId);
        var strategy = strategyRepository.findById(strategyExecution.getStrategyId()).orElseThrow();

        logger.info("Executing strategy: strategyId={}, executionId={}...",
                strategy.getId(), strategyExecution.getId());
        var result = strategyExecutionProcessorManager.getStrategyExecutionProcessor(strategy.getType())
                        .process(strategy, strategyExecution);

        if (result.equals(StrategyExecutionProcessingResult.COMPLETE)) {
            strategyExecutionService.endStrategyExecution(strategyExecution);
        }

        logger.info("Executed strategy: strategyId={}, executionId={}, result={}.",
                strategy.getId(), strategyExecution.getId(), result);
        return result;
    }

}
