package ibez89.tinkoffinvestrobot.api.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Duration;
import java.util.List;

@Schema(description = "Update strategy DTO")
@Data
public final class UpdateStrategyDto {

    @Schema(description = "How often the strategy is executed (ISO 8601 duration)",
            example = "P1D",
            type = "string")
    private final Duration executionPeriod;

    @ArraySchema(schema = @Schema(description = "Instruments (FIGI)", required = true))
    private final List<String> instruments;
}
