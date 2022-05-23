package ibez89.tinkoffinvestrobot.controller;

import ibez89.tinkoffinvestrobot.api.StrategyApi;
import ibez89.tinkoffinvestrobot.api.dto.CreateStrategyDto;
import ibez89.tinkoffinvestrobot.api.dto.StrategyDto;
import ibez89.tinkoffinvestrobot.api.dto.StrategyExecutionDto;
import ibez89.tinkoffinvestrobot.api.dto.UpdateStrategyDto;
import ibez89.tinkoffinvestrobot.model.Strategy;
import ibez89.tinkoffinvestrobot.model.StrategyExecution;
import ibez89.tinkoffinvestrobot.repository.StrategyExecutionSpecification;
import ibez89.tinkoffinvestrobot.service.StrategyExecutionService;
import ibez89.tinkoffinvestrobot.service.StrategyService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class StrategyController implements StrategyApi {

    private final StrategyService strategyService;

    private final StrategyExecutionService strategyExecutionService;

    @Override
    public Page<StrategyDto> getStrategies(Pageable pageable) {
        return convert(strategyService.getStrategies(pageable), Strategy::toDto);
    }

    @Override
    public StrategyDto getStrategy(UUID strategyId) {
        return strategyService.getStrategy(strategyId).toDto();
    }

    @Override
    public StrategyDto updateStrategy(UUID strategyId, UpdateStrategyDto updateStrategyDto) {
        return strategyService.updateStrategy(strategyId, updateStrategyDto).toDto();
    }

    @Override
    public StrategyDto archiveStrategy(UUID strategyId) {
        return strategyService.archiveStrategy(strategyId).toDto();
    }

    @Override
    public Page<StrategyExecutionDto> getStrategyExecutions(
            UUID strategyId, Instant from, Instant to, Pageable pageable) {
        var strategyExecutionsPage = strategyExecutionService.getStrategyExecutions(
                StrategyExecutionSpecification.builder()
                        .strategyIds(Set.of(strategyId))
                        .from(from)
                        .to(to)
                        .build(),
                pageable);
        return convert(strategyExecutionsPage, StrategyExecution::toDto);
    }

    @Override
    public StrategyDto createStrategy(CreateStrategyDto createStrategyDto) {
        return strategyService.createStrategy(createStrategyDto).toDto();
    }

    public static <T, U> Page<U> convert(Page<T> page, Function<T, U> converter) {
        return new PageImpl<>(
                page.getContent().stream()
                        .map(converter)
                        .collect(Collectors.toList()),
                page.getPageable(),
                page.getTotalElements());
    }
}
