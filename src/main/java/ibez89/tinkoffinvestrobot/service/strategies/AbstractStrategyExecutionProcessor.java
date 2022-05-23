package ibez89.tinkoffinvestrobot.service.strategies;

import ibez89.tinkoffinvestrobot.api.model.OrderStatus;
import ibez89.tinkoffinvestrobot.model.Order;
import ibez89.tinkoffinvestrobot.model.Strategy;
import ibez89.tinkoffinvestrobot.tinkoffclient.Portfolio;
import ibez89.tinkoffinvestrobot.tinkoffclient.TinkoffClient;

import java.util.List;
import java.util.Set;

public abstract class AbstractStrategyExecutionProcessor implements StrategyExecutionProcessor {

    private static final Set<OrderStatus> ACTIVE_ORDER_STATUSES = Set.of(OrderStatus.NEW, OrderStatus.PLACED);

    private final TinkoffClient tinkoffClient;

    protected AbstractStrategyExecutionProcessor(TinkoffClient tinkoffClient) {
        this.tinkoffClient = tinkoffClient;
    }

    protected boolean isActive(Order order) {
        return ACTIVE_ORDER_STATUSES.contains(order.getStatus());
    }

    protected boolean hasActiveOrders(List<Order> orders) {
        return orders.stream()
                .anyMatch(this::isActive);
    }

    protected Portfolio getPorfolio(Strategy strategy) {
        var portfolio = tinkoffClient.getPortfolio(strategy.getTinkoffAccountId(), strategy.isSandbox());
        checkNoExtraSecurities(portfolio, strategy);
        return portfolio;
    }

    private void checkNoExtraSecurities(Portfolio portfolio, Strategy strategy) {
        portfolio.getSecurities().stream()
                .filter(position -> !strategy.getInstruments().contains(position.getFigi()))
                .findFirst()
                .ifPresent((position) -> {
                    throw new IllegalStateException("Unexpected position instrument " + position.getFigi());
                });
    }
}
