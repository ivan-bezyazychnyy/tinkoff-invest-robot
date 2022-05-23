package ibez89.tinkoffinvestrobot.api.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Action type", enumAsRef = true)
public enum StrategyType {

    INVEST_FREE_CASH_EQUAL_WEIGHTED,

    REBALANCE_EQUAL_WEIGHTED
}
