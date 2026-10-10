package hamedmoeeni.api_time_travel.adapter.persistence.routing;

import reactor.core.publisher.Mono;

public interface RoutingPersistenceInitiator {
    Mono<Boolean> initiallyLoadRoutes();
}
