package ibez89.tinkoffinvestrobot.tinkoffclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.tinkoff.piapi.contract.v1.*;
import ru.tinkoff.piapi.core.InvestApi;
import ru.tinkoff.piapi.core.models.Positions;
import ru.tinkoff.piapi.core.utils.MapperUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class TinkoffClient {

    private final InvestApi api;

    private final InvestApi sandboxApi;

    public TinkoffClient(
            @Value("${tinkoff-client.app-name}") String appName,
            @Value("${tinkoff-client.token}") String token,
            @Value("${tinkoff-client.sandbox-token") String sandboxToken) {
        this.api = InvestApi.create(token, appName);
        this.sandboxApi = InvestApi.createSandbox(sandboxToken, appName);
    }

    public PriceProvider createCachingPriceProvider() {
        return new CachingPriceProvider(api);
    }

    public Portfolio getPortfolio(String accountId, boolean sandbox) {
        var positions = sandbox ?
                Positions.fromResponse(sandboxApi.getSandboxService().getPositionsSync(accountId)) :
                api.getOperationsService().getPositionsSync(accountId);
        return new Portfolio(positions.getMoney(), positions.getSecurities());
    }

    public Instrument getInstrumentByFigi(String figi) {
        return api.getInstrumentsService().getInstrumentByFigiSync(figi);
    }

    public List<TradingDay> getNearestTradingDays(String exchange) {
        return api.getInstrumentsService()
                .getTradingScheduleSync(
                        exchange,
                        Instant.now(),
                        Instant.now().plus(6, ChronoUnit.DAYS))
                .getDaysList();
    }

    public PostOrderResponse placeMarketOrder(
            String figi, long lots, String tinkoffAccountId, boolean sandbox, OrderDirection orderDirection,
            String orderId) {
        if (sandbox) {
            return sandboxApi.getSandboxService().postOrderSync(
                    figi,
                    lots,
                    MapperUtils.bigDecimalToQuotation(BigDecimal.ZERO),
                    orderDirection,
                    tinkoffAccountId,
                    OrderType.ORDER_TYPE_MARKET,
                    orderId);
        } else {
            return api.getOrdersService().postOrderSync(
                    figi,
                    lots,
                    MapperUtils.bigDecimalToQuotation(BigDecimal.ZERO),
                    orderDirection,
                    tinkoffAccountId,
                    OrderType.ORDER_TYPE_MARKET,
                    orderId);
        }
    }

    public OrderExecutionReportStatus getOrderStatus(
            String tinkoffAccountId, boolean sandbox, String tinkoffOrderId) {
        if (sandbox) {
            return sandboxApi.getSandboxService()
                    .getOrderStateSync(tinkoffAccountId, tinkoffOrderId)
                    .getExecutionReportStatus();
        } else {
            return api.getOrdersService()
                    .getOrderStateSync(tinkoffAccountId, tinkoffOrderId)
                    .getExecutionReportStatus();
        }
    }
}
