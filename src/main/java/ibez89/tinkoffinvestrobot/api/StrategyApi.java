package ibez89.tinkoffinvestrobot.api;

import ibez89.tinkoffinvestrobot.api.dto.CreateStrategyDto;
import ibez89.tinkoffinvestrobot.api.dto.StrategyDto;
import ibez89.tinkoffinvestrobot.api.dto.StrategyExecutionDto;
import ibez89.tinkoffinvestrobot.api.dto.UpdateStrategyDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.converters.models.PageableAsQueryParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@Tag(name = "strategies")
@RequestMapping("/api/v1/strategies")
public interface StrategyApi {

    @PageableAsQueryParam
    @Operation(summary = "Returns all strategies")
    @GetMapping
    Page<StrategyDto> getStrategies(@Parameter(hidden = true) Pageable pageable);

    @Operation(summary = "Returns strategy by id")
    @GetMapping("/{strategyId}")
    StrategyDto getStrategy(@Parameter(description = "Strategy ID") @PathVariable UUID strategyId);

    @Operation(summary = "Updates strategy parameters by id")
    @PutMapping("/{strategyId}")
    StrategyDto updateStrategy(
            @Parameter(description = "Strategy ID") @PathVariable UUID strategyId,
            @Parameter(description = "DTO with new strategy parameters")
            @RequestBody UpdateStrategyDto updateStrategyDto);

    @Operation(summary = "Archives the strategy so no new execution are created")
    @PostMapping("/{strategyId}/archive")
    StrategyDto archiveStrategy(@Parameter(description = "Strategy ID") @PathVariable UUID strategyId);

    @PageableAsQueryParam
    @Operation(summary = "Returns strategy executions with orders satisfying filtering parameters")
    @GetMapping("/{strategyId}/executions")
    Page<StrategyExecutionDto> getStrategyExecutions(
            @Parameter(description = "Strategy ID") @PathVariable UUID strategyId,
            @Parameter(description = "Chooses executions that were active after the passed value")
            @RequestParam(required = false) Instant from,
            @Parameter(description = "Chooses executions that were active until the passed value")
            @RequestParam(required = false) Instant to,
            @Parameter(hidden = true) Pageable pageable);

    @Operation(summary = "Creates new strategy")
    @PostMapping
    StrategyDto createStrategy(
            @Parameter(description = "DTO with strategy description")
            @RequestBody CreateStrategyDto createStrategyDto);
}
