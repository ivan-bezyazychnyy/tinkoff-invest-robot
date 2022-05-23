package ibez89.tinkoffinvestrobot.service;

import ibez89.tinkoffinvestrobot.api.model.OrderDirection;
import ibez89.tinkoffinvestrobot.api.model.OrderStatus;
import ibez89.tinkoffinvestrobot.model.Order;
import ibez89.tinkoffinvestrobot.repository.OrderRepository;
import ibez89.tinkoffinvestrobot.tinkoffclient.TinkoffClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.tinkoff.piapi.contract.v1.Instrument;
import ru.tinkoff.piapi.contract.v1.OrderExecutionReportStatus;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;
import ru.tinkoff.piapi.contract.v1.TradingDay;
import ru.tinkoff.piapi.core.exception.ApiRuntimeException;
import ru.yoomoney.tech.dbqueue.api.EnqueueParams;
import ru.yoomoney.tech.dbqueue.api.QueueProducer;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;

    private final QueueProducer<UUID> orderTaskProducer;

    private final TinkoffClient tinkoffClient;

    public List<Order> getOrdersForStrategyExecution(UUID strategyExecutionId) {
        return orderRepository.findByStrategyExecutionId(strategyExecutionId);
    }

    @Transactional
    public void saveNewOrders(List<Order> orders) {
        orderRepository.saveAll(orders).forEach(order ->
                orderTaskProducer.enqueue(EnqueueParams.create(order.getId())));
    }

    public Order processOrder(UUID orderId) {
        var order = orderRepository.findById(orderId).orElseThrow();
        if (order.getStatus() == OrderStatus.NEW) {
            var instrument = tinkoffClient.getInstrumentByFigi(order.getFigi());
            if (instrument.getTradingStatus().equals(
                    SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING)) {
                placeOrder(order);
            } else {
                order.setPlaceAt(findNextNormalTradingTime(instrument)
                        .orElse(Instant.now().plus(1, ChronoUnit.HOURS)));
            }
        } else {
            checkOrderStatus(order);
        }
        orderRepository.save(order);
        return order;
    }

    private void placeOrder(Order order) {
        try {
            var orderResponse = tinkoffClient.placeMarketOrder(
                    order.getFigi(),
                    order.getLots(),
                    order.getTinkoffAccountId(),
                    order.isSandbox(),
                    order.getDirection().equals(OrderDirection.BUY) ?
                            ru.tinkoff.piapi.contract.v1.OrderDirection.ORDER_DIRECTION_BUY :
                            ru.tinkoff.piapi.contract.v1.OrderDirection.ORDER_DIRECTION_SELL,
                    order.getId().toString());
            logger.info("Got order response: tinkoffOrderStatus={}, orderId={}, tinkoffOrderId={}",
                    orderResponse.getExecutionReportStatus(), order.getId(), orderResponse.getOrderId());
            order.setTinkoffOrderId(orderResponse.getOrderId());
            handleTinkoffOrderStatus(order, orderResponse.getExecutionReportStatus());
            logger.info("Placed order: {}", order);
        } catch (ApiRuntimeException apiRuntimeException) {
            if (apiRuntimeException.getCode().equals("30042")) {
                logger.warn("Not enough money to place order, order is rejected: orderId={}.",
                        order.getId(), apiRuntimeException);
                order.setStatus(OrderStatus.REJECTED);
            } else {
                throw apiRuntimeException;
            }
        }
    }

    private Optional<Instant> findNextNormalTradingTime(Instrument instrument) {
        var now = Instant.now();
        return tinkoffClient.getNearestTradingDays(instrument.getExchange()).stream()
                .filter(TradingDay::getIsTradingDay)
                .sorted(Comparator.comparing(
                        tradingDay -> Instant.ofEpochSecond(tradingDay.getStartTime().getSeconds())))
                .filter(tradingDay -> {
                    var endTime = Instant.ofEpochSecond(tradingDay.getEndTime().getSeconds());
                    return now.plus(5, ChronoUnit.MINUTES).isBefore(endTime);
                })
                .findFirst()
                .map(tradingDay -> {
                    var startTime = Instant.ofEpochSecond(tradingDay.getStartTime().getSeconds());
                    if (now.isAfter(startTime.plus(5, ChronoUnit.MINUTES))) {
                        return now;
                    } else {
                        return startTime.plus(5, ChronoUnit.MINUTES);
                    }
                });
    }

    private void checkOrderStatus(Order order) {
        try {
            var tinkoffOrderStatus = tinkoffClient.getOrderStatus(
                    order.getTinkoffAccountId(), order.isSandbox(), order.getTinkoffOrderId());
            logger.info("Got status for order: tinkoffOrderStatus={}, orderId={}, tinkoffOrderId={}",
                    tinkoffOrderStatus, order.getId(), order.getTinkoffOrderId());
            handleTinkoffOrderStatus(order, tinkoffOrderStatus);
        } catch (ApiRuntimeException apiRuntimeException) {
            if (apiRuntimeException.getCode().equals("50005")) {
                logger.warn("Order is not found: orderId={}.", order.getId(), apiRuntimeException);
                order.setStatus(OrderStatus.UNKNOWN);
            } else {
                throw apiRuntimeException;
            }
        }
        logger.info("Checked order: {}", order);
    }

    private void handleTinkoffOrderStatus(Order order, OrderExecutionReportStatus tinkoffOrderStatus) {
        switch (tinkoffOrderStatus) {
            case EXECUTION_REPORT_STATUS_NEW, EXECUTION_REPORT_STATUS_PARTIALLYFILL ->
                    order.setStatus(OrderStatus.PLACED);
            case EXECUTION_REPORT_STATUS_FILL -> order.setStatus(OrderStatus.FILLED);
            case EXECUTION_REPORT_STATUS_REJECTED -> order.setStatus(OrderStatus.REJECTED);
            default -> throw new IllegalStateException("Unexpected status from tinkoff: " + tinkoffOrderStatus);
        }
    }
}
