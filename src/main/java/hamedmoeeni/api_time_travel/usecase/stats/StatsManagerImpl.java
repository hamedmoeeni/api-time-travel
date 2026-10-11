package hamedmoeeni.api_time_travel.usecase.stats;

import hamedmoeeni.api_time_travel.domain.stats.Stats;
import hamedmoeeni.api_time_travel.usecase.stats.port.StatsDataCollector;
import hamedmoeeni.api_time_travel.usecase.stats.port.StatsPersistence;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
class StatsManagerImpl implements StatsManger {
    private final StatsDataCollector statsDataCollector;
    private final StatsPersistence statsPersistence;

    public StatsManagerImpl(StatsDataCollector statsDataCollector, StatsPersistence statsPersistence) {
        this.statsDataCollector = statsDataCollector;
        this.statsPersistence = statsPersistence;
    }

    @Override
    public Mono<Boolean> enable(boolean enable) {
        return statsDataCollector.enable(enable);
    }

    @Override
    public Mono<Boolean> enablePlayback(boolean enable) {
        return statsDataCollector.enablePlayback(enable);
    }

    @Override
    public Mono<Boolean> saveStats(Stats stats) {
        return statsPersistence.saveStats(stats);
    }

    @Override
    public Mono<Boolean> updateStatsAggregates() {
        return null;
    }
}
