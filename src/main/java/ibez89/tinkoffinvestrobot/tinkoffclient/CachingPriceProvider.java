package ibez89.tinkoffinvestrobot.tinkoffclient;

import lombok.RequiredArgsConstructor;
import ru.tinkoff.piapi.core.InvestApi;
import ru.tinkoff.piapi.core.utils.MapperUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class CachingPriceProvider implements PriceProvider {

    private final InvestApi api;

    private final Map<String, BigDecimal> prices = new ConcurrentHashMap<>();

    @Override
    public BigDecimal getPrice(String figi) {
        return prices.computeIfAbsent(figi, ignored ->
                MapperUtils.quotationToBigDecimal(
                        api.getMarketDataService().getLastPricesSync(List.of(figi))
                                .stream()
                                .findFirst()
                                .orElseThrow(() -> new IllegalStateException(
                                        "Could not get price for instrument with FIGI " + figi))
                                .getPrice())
        );
    }
}
