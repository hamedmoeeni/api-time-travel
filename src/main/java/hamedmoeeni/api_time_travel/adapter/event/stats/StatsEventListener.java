package hamedmoeeni.api_time_travel.adapter.event.stats;

import hamedmoeeni.api_time_travel.domain.stats.Stats;
import reactor.core.publisher.Mono;

public interface StatsEventListener {
    Mono<Boolean> onStats(Stats stats);

}
