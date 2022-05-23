package ibez89.tinkoffinvestrobot.api.dto;

import lombok.Builder;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Builder
public record StrategyExecutionDto(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        UUID strategyId,
        String tinkoffAccountId,
        boolean sandbox,
        Instant startedAt,
        Instant endedAt,
        List<OrderDto> orders) {
}
