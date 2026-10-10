package hamedmoeeni.api_time_travel.usecase.stats.port;

import reactor.core.publisher.Mono;

public interface StatsDataCollector {
    Mono<Boolean> enable(boolean enable);

    Mono<Boolean> enablePlayback(boolean enable);

}
