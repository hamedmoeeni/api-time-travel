package hamedmoeeni.api_time_travel.adapter.routing;

import hamedmoeeni.api_time_travel.usecase.routing.Route;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface RoutingInfrastructure {
    Mono<?> updateRoutes(Collection<Route> routes);

    Flux<Route> getRoutes();
}
