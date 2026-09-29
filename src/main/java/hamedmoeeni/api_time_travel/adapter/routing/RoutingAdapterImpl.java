package hamedmoeeni.api_time_travel.adapter.routing;

import hamedmoeeni.api_time_travel.usecase.routing.Route;
import hamedmoeeni.api_time_travel.usecase.routing.RoutingAdapter;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Service
public class RoutingAdapterImpl implements RoutingAdapter {
    private final RoutingInfrastructure routingInfrastructure;

    public RoutingAdapterImpl(RoutingInfrastructure routingInfrastructure) {
        this.routingInfrastructure = routingInfrastructure;
    }

    @Override
    public Mono<?> updateRoutes(Collection<Route> routes) {
        return routingInfrastructure.updateRoutes(routes);
    }

    @Override
    public Flux<Route> getRoutes() {
        return routingInfrastructure.getRoutes();
    }
}
