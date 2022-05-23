package ibez89.tinkoffinvestrobot.api.dto;

import ibez89.tinkoffinvestrobot.api.model.StrategyType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Duration;
import java.util.List;

@Schema(description = "Create strategy DTO")
@Data
public final class CreateStrategyDto {

    @Schema(description = "Tinkoff account id")
    private final String tinkoffAccountId;

    @Schema(description = "Flag that indicates that account is from sandbox environment")
    private final boolean sandbox;

    @Schema(description = "Strategy typ")
    private final StrategyType type;

    @Schema(description = "How often the strategy is executed (ISO 8601 duration)", example = "P1D", type = "string")
    private final Duration executionPeriod;

    @ArraySchema(schema = @Schema(description = "Instruments (FIGI)", required = true))
    private final List<String> instruments;
}
