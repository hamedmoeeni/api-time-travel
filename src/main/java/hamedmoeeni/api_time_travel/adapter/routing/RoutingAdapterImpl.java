package hamedmoeeni.api_time_travel.adapter.routing;

import hamedmoeeni.api_time_travel.adapter.persistence.routing.RouteRepository;
import hamedmoeeni.api_time_travel.usecase.routing.Route;
import hamedmoeeni.api_time_travel.usecase.routing.RoutingAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Service
public class RoutingAdapterImpl implements RoutingAdapter {
    private final Logger logger = LoggerFactory.getLogger(RoutingAdapterImpl.class);
    private final RoutingInfrastructure routingInfrastructure;
    private final RouteRepository routeRepository;

    public RoutingAdapterImpl(RoutingInfrastructure routingInfrastructure, RouteRepository routeRepository) {
        this.routingInfrastructure = routingInfrastructure;
        this.routeRepository = routeRepository;
    }

    @Override
    public Mono<Boolean> initiallyLoadRoutes() {
        return routeRepository.findAllRoutes()
                .collectList()
                .flatMap(routingInfrastructure::saveRoutes);
    }

    @Override
    public Mono<Boolean> updateRoutes(Collection<Route> routes) {
        return routingInfrastructure.saveRoutes(routes)
                .then(routeRepository.saveRoutes(routes))
                .doOnError(throwable -> logger.error("Error persisting routes", throwable));
    }

    @Override
    public Flux<Route> getRoutes() {
        return routingInfrastructure.getRoutes();
    }

    @Override
    public Mono<Boolean> deleteRoutes(Collection<String> routeIds) {
        return routeRepository.deleteById(routeIds)
                .then(routingInfrastructure.removeRoutes(routeIds))
                .doOnError(throwable -> logger.error("Error deleting routes", throwable));
    }
}
