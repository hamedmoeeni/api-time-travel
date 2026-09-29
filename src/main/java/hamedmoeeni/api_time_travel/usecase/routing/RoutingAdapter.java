package hamedmoeeni.api_time_travel.usecase.routing;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

public interface RoutingAdapter {
    Mono<?> updateRoutes(Collection<Route> routes);

    Flux<Route> getRoutes();
}
