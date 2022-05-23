package ibez89.tinkoffinvestrobot.service;

import ibez89.tinkoffinvestrobot.model.StrategyExecution;
import ibez89.tinkoffinvestrobot.repository.StrategyExecutionRepository;
import ibez89.tinkoffinvestrobot.repository.StrategyExecutionSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yoomoney.tech.dbqueue.api.EnqueueParams;
import ru.yoomoney.tech.dbqueue.api.QueueProducer;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StrategyExecutionService {

    private final StrategyExecutionRepository strategyExecutionRepository;

    private final QueueProducer<UUID> strategyExecutionTaskProducer;

    public StrategyExecution getStrategyExecutionById(UUID strategyExecutionId) {
        return strategyExecutionRepository.findById(strategyExecutionId).orElseThrow();
    }

    public Page<StrategyExecution> getStrategyExecutions(
            StrategyExecutionSpecification specification, Pageable pageable) {
        return strategyExecutionRepository.findAll(specification, pageable);
    }

    @Transactional
    public StrategyExecution createStrategyExecution(StrategyExecution strategyExecution) {
        var createdStrategyExecution = strategyExecutionRepository.save(strategyExecution);
        strategyExecutionTaskProducer.enqueue(EnqueueParams.create(createdStrategyExecution.getId()));
        return createdStrategyExecution;
    }

    @Transactional
    public void endStrategyExecution(StrategyExecution strategyExecution) {
        strategyExecution.setEndedAt(Instant.now());
        strategyExecutionRepository.save(strategyExecution);
    }
}
