package ibez89.tinkoffinvestrobot.tinkoffclient;

import com.google.protobuf.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.tinkoff.piapi.contract.v1.*;
import ru.tinkoff.piapi.core.InvestApi;
import ru.tinkoff.piapi.core.models.Positions;
import ru.tinkoff.piapi.core.utils.MapperUtils;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

@Component
public class TinkoffClient {

    private final static Logger logger = LoggerFactory.getLogger(TinkoffClient.class);

    private final InvestApi api;

    private final InvestApi sandboxApi;

    public TinkoffClient(
            @Value("${tinkoff-client.app-name}") String appName,
            @Value("${tinkoff-client.token}") String token,
            @Value("${tinkoff-client.sandbox-token") String sandboxToken) {
        this.api = InvestApi.create(token, appName);
        this.sandboxApi = InvestApi.createSandbox(sandboxToken, appName);
    }

    @PostConstruct
    public void init() {
        var instruments = Set.of("GAZP", "SBERP", "MTSS", "ROSN", "RTKM", "DSKY", "MOEX");
        api.getInstrumentsService().getAllSharesSync()
                .stream()
                .filter(share -> instruments.contains(share.getTicker()))
                .limit(10)
                .forEach(share -> {
                    System.out.printf("Share: ticker=%s, classCode=%s, figi=%s, exchange=%s, lotSize=%s%n",
                            share.getTicker(), share.getClassCode(), share.getFigi(), share.getExchange(),
                            share.getLot());
                    api.getInstrumentsService().getTradingScheduleSync(share.getExchange(), Instant.now(),
                                    Instant.now().plus(6, ChronoUnit.DAYS))
                            .getDaysList()
                            .forEach(tradingDay ->
                                    System.out.printf("Trading day: %s, %s, %s, %s, %s, %s, %s%n",
                                            toInstant(tradingDay.getDate()),
                                            tradingDay.getIsTradingDay(),
                                            toInstant(tradingDay.getStartTime()),
                                            toInstant(tradingDay.getEndTime()),
                                            toInstant(tradingDay.getOpeningAuctionStartTime()),
                                            toInstant(tradingDay.getPremarketStartTime()),
                                            toInstant(tradingDay.getPremarketEndTime())));
                });
        System.out.println();
    }

    private static Instant toInstant(Timestamp timestamp) {
        return Instant.ofEpochSecond(timestamp.getSeconds());
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
