package ibez89.tinkoffinvestrobot.service.strategies;

import ibez89.tinkoffinvestrobot.api.model.StrategyType;
import ibez89.tinkoffinvestrobot.model.Strategy;
import ibez89.tinkoffinvestrobot.model.StrategyExecution;
import ibez89.tinkoffinvestrobot.service.OrderService;
import ibez89.tinkoffinvestrobot.service.StrategyExecutionProcessingResult;
import ibez89.tinkoffinvestrobot.tinkoffclient.TinkoffClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class InvestFreeCashEqualWeightedStrategyExecutionProcessor extends AbstractStrategyExecutionProcessor {

    private static final Logger logger =
            LoggerFactory.getLogger(InvestFreeCashEqualWeightedStrategyExecutionProcessor.class);

    private final EqualWeightedAlgorithmOrderCreator equalWeightedAlgorithmOrderCreator;

    private final OrderService orderService;

    public InvestFreeCashEqualWeightedStrategyExecutionProcessor(
            EqualWeightedAlgorithmOrderCreator equalWeightedAlgorithmOrderCreator,
            OrderService orderService,
            TinkoffClient tinkoffClient) {
        super(tinkoffClient);
        this.equalWeightedAlgorithmOrderCreator = equalWeightedAlgorithmOrderCreator;
        this.orderService = orderService;
    }

    @Override
    public StrategyType getStrategyType() {
        return StrategyType.INVEST_FREE_CASH_EQUAL_WEIGHTED;
    }

    @Override
    public StrategyExecutionProcessingResult process(Strategy strategy, StrategyExecution strategyExecution) {
        var currentOrders = orderService.getOrdersForStrategyExecution(strategyExecution.getId());
        if (!currentOrders.isEmpty()) {
            if (!hasActiveOrders(currentOrders)) {
                logger.info("Completing strategy execution with orders in final state: " +
                                "strategyExecutionId={}, orders={}.",
                        strategyExecution.getId(), currentOrders);
                return StrategyExecutionProcessingResult.COMPLETE;
            } else {
                logger.info("Rescheduling strategy execution with orders not in final state: " +
                                "strategyExecutionId={}, orders={}.",
                        strategyExecution.getId(), currentOrders);
                return StrategyExecutionProcessingResult.RESCHEDULE;
            }
        }

        var portfolio = getPorfolio(strategy);
        var newOrders = equalWeightedAlgorithmOrderCreator.createBuyOptimizationOrders(
                strategyExecution, portfolio, strategy.getInstruments());
        logger.info("Generated orders for strategy execution: id={}, orders={}",
                strategyExecution.getId(), newOrders);
        if (!newOrders.isEmpty()) {
            orderService.saveNewOrders(newOrders);
            return StrategyExecutionProcessingResult.RESCHEDULE;
        }

        return StrategyExecutionProcessingResult.COMPLETE;
    }
}