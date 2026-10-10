package hamedmoeeni.api_time_travel.adapter.collector.stats;

import hamedmoeeni.api_time_travel.adapter.collector.stats.port.StatsDataCollectorInfrastructure;
import hamedmoeeni.api_time_travel.usecase.stats.port.StatsDataCollector;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
class StatsDataCollectorImpl implements StatsDataCollector {
    private final StatsDataCollectorInfrastructure statsDataCollectorInfrastructure;

    public StatsDataCollectorImpl(StatsDataCollectorInfrastructure statsDataCollectorInfrastructure) {
        this.statsDataCollectorInfrastructure = statsDataCollectorInfrastructure;
    }

    @Override
    public Mono<Boolean> enable(boolean enable) {
        return statsDataCollectorInfrastructure.enable(enable);
    }

    @Override
    public Mono<Boolean> enablePlayback(boolean enable) {
        return statsDataCollectorInfrastructure.enablePlayback(enable);
    }
}
