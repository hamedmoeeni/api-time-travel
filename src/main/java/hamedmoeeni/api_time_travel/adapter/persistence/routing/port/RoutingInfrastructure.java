package hamedmoeeni.api_time_travel.adapter.persistence.routing.port;

import hamedmoeeni.api_time_travel.domain.routing.Route;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface RoutingInfrastructure {
    Mono<Boolean> saveRoutes(Collection<Route> routes);

    Flux<Route> getRoutes();

    Mono<Boolean> removeRoutes(Collection<String> routeIds);
}
