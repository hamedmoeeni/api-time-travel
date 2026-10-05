package hamedmoeeni.api_time_travel.infrastructure.routing;

import hamedmoeeni.api_time_travel.usecase.routing.Route;
import org.mapstruct.Mapper;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring")
public interface RouteDefinitionMapper {

    default RouteDefinition toRouteDefinition(Route route) {
        if (route == null) {
            return null;
        }
        RouteDefinition routeDefinition = new RouteDefinition();
        routeDefinition.setId(route.getId() != null && !route.getId().isBlank() ? route.getId() : UUID.randomUUID().toString());
        routeDefinition.setOrder(route.getPriority());
        routeDefinition.setUri(URI.create(route.getDestinationHostUri()));
        List<PredicateDefinition> predicates = new ArrayList<>();
        if (route.getSourcePath() != null && !route.getSourcePath().isBlank()) {
            predicates.add(new PredicateDefinition("Path=" + route.getSourcePath()));
        }
        if (route.getMethod() != null && !route.getMethod().isBlank()) {
            predicates.add(new PredicateDefinition("Method=" + route.getMethod().toUpperCase()));
        }
        if (route.getSourceHost() != null && !route.getSourceHost().isBlank()) {
            predicates.add(new PredicateDefinition("Host=" + route.getSourceHost()));
        }
        if (!predicates.isEmpty()) {
            routeDefinition.setPredicates(predicates);
        }
        return routeDefinition;
    }

    default Route fromRouteDefinition(RouteDefinition routeDefinition) {
        if (routeDefinition == null) {
            return null;
        }
        Route route = new Route();
        route.setId(routeDefinition.getId());
        route.setPriority(routeDefinition.getOrder());
        route.setDestinationHostUri(routeDefinition.getUri() == null ? null : routeDefinition.getUri().toString());
        routeDefinition.getPredicates().forEach(predicateDefinition -> {
            if (predicateDefinition.getName() == null)
                return;
            if (predicateDefinition.getName() != null && predicateDefinition.getName().equalsIgnoreCase("path")) {
                route.setSourcePath(String.join(",", predicateDefinition.getArgs().values()));
            }
            if (predicateDefinition.getName() != null && predicateDefinition.getName().equalsIgnoreCase("method")) {
                route.setMethod(String.join(",", predicateDefinition.getArgs().values()));
            }
            if (predicateDefinition.getName() != null && predicateDefinition.getName().equalsIgnoreCase("host")) {
                route.setSourceHost(String.join(",", predicateDefinition.getArgs().values()));
            }
        });
        return route;
    }

}
