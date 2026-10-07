package hamedmoeeni.api_time_travel.usecase.stats;

import reactor.core.publisher.Mono;

public interface StatsManger {
    Mono<Boolean> enable(boolean enable);
    Mono<Boolean> enablePlayback(boolean enable);
}
