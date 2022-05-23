package ibez89.tinkoffinvestrobot.api.dto;

import ibez89.tinkoffinvestrobot.api.model.StrategyStatus;
import ibez89.tinkoffinvestrobot.api.model.StrategyType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Schema(description = "Strategy DTO")
@Builder
public record StrategyDto(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        StrategyStatus status,

        @Schema(description = "Tinkoff account id")
        String tinkoffAccountId,

        @Schema(description = "Flag that indicates that account is from sandbox environment")
        boolean sandbox,

        @Schema(description = "Strategy typ")
        StrategyType type,

        @Schema(description = "How often the strategy is executed (ISO 8601 duration)",
                example = "P1D",
                type = "string")
        Duration executionPeriod,

        @ArraySchema(schema = @Schema(description = "Instruments (FIGI)", required = true))
        List<String> instruments) {
}
