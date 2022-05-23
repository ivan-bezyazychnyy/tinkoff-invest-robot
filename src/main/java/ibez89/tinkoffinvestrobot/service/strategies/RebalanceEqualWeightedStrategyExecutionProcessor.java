package ibez89.tinkoffinvestrobot.service.strategies;

import ibez89.tinkoffinvestrobot.api.model.OrderDirection;
import ibez89.tinkoffinvestrobot.api.model.StrategyType;
import ibez89.tinkoffinvestrobot.model.Order;
import ibez89.tinkoffinvestrobot.model.Strategy;
import ibez89.tinkoffinvestrobot.model.StrategyExecution;
import ibez89.tinkoffinvestrobot.service.OrderService;
import ibez89.tinkoffinvestrobot.service.StrategyExecutionProcessingResult;
import ibez89.tinkoffinvestrobot.tinkoffclient.TinkoffClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RebalanceEqualWeightedStrategyExecutionProcessor extends AbstractStrategyExecutionProcessor {

    private static final Logger logger =
            LoggerFactory.getLogger(RebalanceEqualWeightedStrategyExecutionProcessor.class);

    private final EqualWeightedAlgorithmOrderCreator equalWeightedAlgorithmOrderCreator;

    private final OrderService orderService;

    public RebalanceEqualWeightedStrategyExecutionProcessor(
            EqualWeightedAlgorithmOrderCreator equalWeightedAlgorithmOrderCreator,
            OrderService orderService,
            TinkoffClient tinkoffClient) {
        super(tinkoffClient);
        this.equalWeightedAlgorithmOrderCreator = equalWeightedAlgorithmOrderCreator;
        this.orderService = orderService;
    }

    @Override
    public StrategyType getStrategyType() {
        return StrategyType.REBALANCE_EQUAL_WEIGHTED;
    }

    /**
     * The processing is performed in two stages:
     * 1. SELL orders are placed if required to create free cash.
     * 2. BUY orders are created when SELL orders are filled.
     */
    @Override
    public StrategyExecutionProcessingResult process(Strategy strategy, StrategyExecution strategyExecution) {

        var orders = orderService.getOrdersForStrategyExecution(strategyExecution.getId());

        if (hasActiveOrders(orders)) {
            logger.info("Rescheduling strategy execution with orders not in final state: " +
                            "strategyExecutionId={}, orders={}.",
                    strategyExecution.getId(), orders);
            return StrategyExecutionProcessingResult.RESCHEDULE;
        }

        if (hasBuyOrders(orders)) {
            // both sell and buy stages are passed
            return StrategyExecutionProcessingResult.COMPLETE;
        }

        var portfolio = getPorfolio(strategy);

        if (!hasSellOrders(orders)) {
            // An attempt to create sell orders
            var newSellOrders = equalWeightedAlgorithmOrderCreator.createSellOptimizationOrders(
                    strategyExecution, portfolio, strategy.getInstruments());
            if (!newSellOrders.isEmpty()) {
                logger.info("Generated sell orders for strategy execution: id={}, orders={}",
                        strategyExecution.getId(), newSellOrders);
                orderService.saveNewOrders(newSellOrders);
                return StrategyExecutionProcessingResult.RESCHEDULE;
            }
        }

        var newBuyOrders = equalWeightedAlgorithmOrderCreator.createBuyOptimizationOrders(
                strategyExecution, portfolio, strategy.getInstruments());
        if (!newBuyOrders.isEmpty()) {
            logger.info("Generated buy orders for strategy execution: id={}, orders={}",
                    strategyExecution.getId(), newBuyOrders);
            orderService.saveNewOrders(newBuyOrders);
            return StrategyExecutionProcessingResult.RESCHEDULE;
        }

        return StrategyExecutionProcessingResult.COMPLETE;
    }

    private boolean hasBuyOrders(List<Order> orders) {
        return orders.stream()
                .anyMatch(order -> order.getDirection().equals(OrderDirection.BUY));
    }

    private boolean hasSellOrders(List<Order> orders) {
        return orders.stream()
                .anyMatch(order -> order.getDirection().equals(OrderDirection.SELL));
    }
}
