package hamedmoeeni.api_time_travel.adapter.persistence.routing;

import hamedmoeeni.api_time_travel.usecase.routing.Route;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface RouteRepository {
    Flux<Route> findAllRoutes();

    Mono<Boolean> saveRoutes(Collection<Route> routes);

    Mono<Boolean> deleteById(Collection<String> routeIds);
}
