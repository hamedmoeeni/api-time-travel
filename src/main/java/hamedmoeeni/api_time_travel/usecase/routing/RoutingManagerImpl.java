package hamedmoeeni.api_time_travel.usecase.routing;

import hamedmoeeni.api_time_travel.domain.routing.Route;
import hamedmoeeni.api_time_travel.usecase.routing.port.RoutingPersistence;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Service
class RoutingManagerImpl implements RoutingManager {

    private final RoutingPersistence routingPersistence;

    RoutingManagerImpl(RoutingPersistence routingPersistence) {
        this.routingPersistence = routingPersistence;
    }

    @Override
    public Mono<Boolean> updateRoutes(Collection<Route> routes) {
        return routingPersistence.updateRoutes(routes);
    }

    @Override
    public Flux<Route> getRoutes() {
        return routingPersistence.getRoutes();
    }

    @Override
    public Mono<Boolean> deleteRoutes(Collection<String> routeIds) {
        return routingPersistence.deleteRoutes(routeIds);
    }
}
