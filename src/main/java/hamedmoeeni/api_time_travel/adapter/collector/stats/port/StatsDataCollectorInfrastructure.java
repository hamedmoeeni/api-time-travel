package hamedmoeeni.api_time_travel.adapter.collector.stats.port;

import reactor.core.publisher.Mono;

public interface StatsDataCollectorInfrastructure {
    Mono<Boolean> enable(boolean enable);

    Mono<Boolean> enablePlayback(boolean enable);
}
