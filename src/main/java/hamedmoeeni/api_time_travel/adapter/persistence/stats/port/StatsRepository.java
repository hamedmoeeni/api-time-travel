package hamedmoeeni.api_time_travel.adapter.persistence.stats.port;

import hamedmoeeni.api_time_travel.domain.stats.Stats;
import reactor.core.publisher.Mono;

public interface StatsRepository {
    Mono<Boolean> upsertIgnoreResponse(Stats stats);

    Mono<Boolean> upsertIgnoreRequest(Stats stats);
}
