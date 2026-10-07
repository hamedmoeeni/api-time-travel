package hamedmoeeni.api_time_travel.adapter.stats;

import hamedmoeeni.api_time_travel.infrastructure.filter.StatsFilter;
import hamedmoeeni.api_time_travel.usecase.stats.StatsAdapter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class StatsAdapterImpl implements StatsAdapter {
    private final StatsFilter statsFilter;

    public StatsAdapterImpl(StatsFilter statsFilter) {
        this.statsFilter = statsFilter;
    }

    @Override
    public Mono<Boolean> enable(boolean enable) {
        return Mono.fromCallable(() -> {
            statsFilter.setBypassed(!enable);
            return true;
        });
    }

    @Override
    public Mono<Boolean> enablePlayback(boolean enable) {
        return Mono.fromCallable(() -> {
            statsFilter.setStoreBodies(enable);
            return true;
        });
    }
}
