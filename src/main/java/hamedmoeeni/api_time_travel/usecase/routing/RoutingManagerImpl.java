package hamedmoeeni.api_time_travel.usecase.routing;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Service
class RoutingManagerImpl implements RoutingManager {

    private final RoutingAdapter routingAdapter;

    RoutingManagerImpl(RoutingAdapter routingAdapter) {
        this.routingAdapter = routingAdapter;
    }

    @Override
    public Mono<Boolean> updateRoutes(Collection<Route> routes) {
        return routingAdapter.updateRoutes(routes);
    }

    @Override
    public Flux<Route> getRoutes() {
        return routingAdapter.getRoutes();
    }

    @Override
    public Mono<Boolean> deleteRoutes(Collection<String> routeIds) {
        return routingAdapter.deleteRoutes(routeIds);
    }
}
