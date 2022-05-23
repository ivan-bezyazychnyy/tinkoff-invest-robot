package ibez89.tinkoffinvestrobot.service.strategies;

import ibez89.tinkoffinvestrobot.api.model.OrderDirection;
import ibez89.tinkoffinvestrobot.api.model.OrderStatus;
import ibez89.tinkoffinvestrobot.model.Order;
import ibez89.tinkoffinvestrobot.model.StrategyExecution;
import ibez89.tinkoffinvestrobot.tinkoffclient.Portfolio;
import ibez89.tinkoffinvestrobot.tinkoffclient.TinkoffClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.tinkoff.piapi.contract.v1.Instrument;
import ru.tinkoff.piapi.core.models.SecurityPosition;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EqualWeightedAlgorithmOrderCreator {

    private static final Logger logger = LoggerFactory.getLogger(EqualWeightedAlgorithmOrderCreator.class);

    /**
     * Reserve ratio is used to take price fluctuation and broker commission into account.
     */
    private static final BigDecimal RESERVE_RATIO = BigDecimal.valueOf(1.03);

    private final TinkoffClient tinkoffClient;

    public List<Order> createBuyOptimizationOrders(
            StrategyExecution strategyExecution, Portfolio portfolio, List<String> instrumentNames) {
        return createOrdersPerCurrency(instrumentNames,
                (currency, instruments) -> createBuyOptimizationOrdersForCommonCurrency(
                        strategyExecution,
                        portfolio.getMoneyWithCurrency(currency),
                        portfolio.getPositionsForInstruments(
                                instruments.stream()
                                        .map(Instrument::getFigi)
                                        .collect(Collectors.toSet())),
                        instruments));
    }

    public List<Order> createSellOptimizationOrders(
            StrategyExecution strategyExecution, Portfolio portfolio, List<String> instrumentNames) {
        return createOrdersPerCurrency(instrumentNames,
                (currency, instruments) -> createSellOptimizationOrdersForCommonCurrency(
                        strategyExecution,
                        portfolio.getMoneyWithCurrency(currency),
                        portfolio.getPositionsForInstruments(
                                instruments.stream()
                                        .map(Instrument::getFigi)
                                        .collect(Collectors.toSet())),
                        instruments));
    }

    private List<Order> createOrdersPerCurrency(
            List<String> instrumentNames,
            BiFunction<String, List<Instrument>, List<Order>> currencyInstrumentsToOrdersFunction) {
        var orders = new ArrayList<Order>();
        instrumentNames.stream()
                .map(tinkoffClient::getInstrumentByFigi)
                .collect(Collectors.groupingBy(Instrument::getCurrency))
                .forEach((currency, instruments) ->
                        orders.addAll(currencyInstrumentsToOrdersFunction.apply(currency, instruments)));
        return orders;
    }

    private List<Order> createBuyOptimizationOrdersForCommonCurrency(
            StrategyExecution strategyExecution,
            BigDecimal money,
            List<SecurityPosition> currentPositions,
            List<Instrument> instruments) {
        if (instruments.isEmpty()) {
            return Collections.emptyList();
        }

        var pricedPositions = createPricedPositions(instruments, currentPositions);
        var totalValue = calculateTotalValue(money, pricedPositions);
        var targetPositionValue = totalValue.divide(BigDecimal.valueOf(instruments.size()), RoundingMode.DOWN);

        logger.info("Creating buy orders for strategy execution: " +
                        "id={}, money={}, pricedPositions={}, totalValue={}, targetPositionValue={}",
                strategyExecution.getId(), money, pricedPositions, totalValue, targetPositionValue);

        var pricedPositionsSortedByInsufficiencyWithTargetValueDesc = pricedPositions.stream()
                .sorted((position1, position2) ->
                        position2.calculateInsufficiency(targetPositionValue).compareTo(position1.calculateInsufficiency(targetPositionValue)))
                .toList();

        var orders = new ArrayList<Order>();
        var freeMoney = money;
        for (var pricedPosition : pricedPositionsSortedByInsufficiencyWithTargetValueDesc) {
            var lotSize = pricedPosition.getInstrument().getLot();
            var lotPrice = pricedPosition.getPrice()
                    .multiply(BigDecimal.valueOf(lotSize)
                            .multiply(RESERVE_RATIO));
            var insufficiency = pricedPosition.calculateInsufficiency(targetPositionValue);
            if (insufficiency.compareTo(lotPrice) >= 0 && freeMoney.compareTo(lotPrice) >= 0) {
                var lots = min(insufficiency, freeMoney)
                        .divide(lotPrice, RoundingMode.DOWN)
                        .longValue();
                orders.add(Order.builder()
                        .tinkoffAccountId(strategyExecution.getTinkoffAccountId())
                        .sandbox(strategyExecution.isSandbox())
                        .strategyExecutionId(strategyExecution.getId())
                        .figi(pricedPosition.getInstrument().getFigi())
                        .lots(lots)
                        .direction(OrderDirection.BUY)
                        .status(OrderStatus.NEW)
                        .build());
                freeMoney = freeMoney.subtract(lotPrice.multiply(BigDecimal.valueOf(lots)));
            } else {
                break;
            }
        }

        return orders;
    }

    private static BigDecimal min(BigDecimal value1, BigDecimal value2) {
        return value1.compareTo(value2) <= 0 ? value1 : value2;
    }

    private List<Order> createSellOptimizationOrdersForCommonCurrency(
            StrategyExecution strategyExecution,
            BigDecimal money,
            List<SecurityPosition> currentPositions,
            List<Instrument> instruments) {
        if (instruments.isEmpty()) {
            return Collections.emptyList();
        }

        var pricedPositions = createPricedPositions(instruments, currentPositions);
        var totalValue = calculateTotalValue(money, pricedPositions);
        var targetPositionValue = totalValue.divide(BigDecimal.valueOf(instruments.size()), RoundingMode.UP);

        logger.info("Creating sell orders for strategy execution: " +
                        "id={}, money={}, pricedPositions={}, totalValue={}, targetPositionValue={}",
                strategyExecution.getId(), money, pricedPositions, totalValue, targetPositionValue);

        var pricedPositionsSortedByExcessWithTargetValueDesc = pricedPositions.stream()
                .sorted((position1, position2) ->
                        position2.calculateExcess(targetPositionValue).compareTo(position1.calculateExcess(targetPositionValue)))
                .toList();

        var orders = new ArrayList<Order>();
        for (var pricedPosition : pricedPositionsSortedByExcessWithTargetValueDesc) {
            var lotSize = pricedPosition.getInstrument().getLot();
            var lotPrice = pricedPosition.getPrice()
                    .multiply(BigDecimal.valueOf(lotSize)
                            .multiply(RESERVE_RATIO));
            if (pricedPosition.calculateExcess(targetPositionValue).compareTo(lotPrice) >= 0) {
                var lots = pricedPosition.calculateExcess(targetPositionValue)
                        .divide(lotPrice, RoundingMode.DOWN)
                        .longValue();
                orders.add(Order.builder()
                        .tinkoffAccountId(strategyExecution.getTinkoffAccountId())
                        .sandbox(strategyExecution.isSandbox())
                        .strategyExecutionId(strategyExecution.getId())
                        .figi(pricedPosition.getInstrument().getFigi())
                        .lots(lots)
                        .direction(OrderDirection.SELL)
                        .status(OrderStatus.NEW)
                        .build());
            } else {
                break;
            }
        }

        return orders;
    }

    private List<PricedPosition> createPricedPositions(
            List<Instrument> instruments, List<SecurityPosition> currentPositions) {
        var priceProvider = tinkoffClient.createCachingPriceProvider();
        var currentPositionByFigi = currentPositions.stream()
                .collect(Collectors.toMap(SecurityPosition::getFigi, Function.identity()));
        return instruments.stream()
                .map(instrument -> {
                    var pricedPositionBuilder = PricedPosition.builder()
                            .instrument(instrument)
                            .price(priceProvider.getPrice(instrument.getFigi()));
                    if (currentPositionByFigi.containsKey(instrument.getFigi())) {
                        pricedPositionBuilder.quantity(currentPositionByFigi.get(instrument.getFigi()).getBalance());
                    } else {
                        pricedPositionBuilder.quantity(0L);
                    }
                    return pricedPositionBuilder.build();
                })
                .collect(Collectors.toList());
    }

    private BigDecimal calculateTotalValue(BigDecimal money, List<PricedPosition> pricedPositions) {
        return pricedPositions.stream()
                .map(PricedPosition::getValue)
                .reduce(money, BigDecimal::add);
    }
}
