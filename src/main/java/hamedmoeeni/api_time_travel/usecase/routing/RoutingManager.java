package hamedmoeeni.api_time_travel.usecase.routing;

import hamedmoeeni.api_time_travel.domain.routing.Route;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface RoutingManager {
    Mono<Boolean> updateRoutes(Collection<Route> routes);

    Flux<Route> getRoutes();

    Mono<Boolean> deleteRoutes(Collection<String> routeIds);
}
