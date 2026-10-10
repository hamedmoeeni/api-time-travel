package hamedmoeeni.api_time_travel.usecase.stats.port;

import hamedmoeeni.api_time_travel.domain.stats.Stats;
import reactor.core.publisher.Mono;

public interface StatsPersistence {
    Mono<Boolean> saveStats(Stats stats);
}
