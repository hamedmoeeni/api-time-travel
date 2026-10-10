package hamedmoeeni.api_time_travel.usecase.stats;

import hamedmoeeni.api_time_travel.domain.stats.Stats;
import reactor.core.publisher.Mono;

public interface StatsManger {
    Mono<Boolean> enable(boolean enable);

    Mono<Boolean> enablePlayback(boolean enable);

    Mono<Boolean> saveStats(Stats stats);

    Mono<Boolean> updateStatsAggregates();
}
