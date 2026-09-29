package hamedmoeeni.api_time_travel.infrastructure.routing;

import hamedmoeeni.api_time_travel.adapter.routing.RoutingInfrastructure;
import hamedmoeeni.api_time_travel.usecase.routing.Route;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;

@Component
public class RoutingInfrastructureImpl implements RoutingInfrastructure, ApplicationEventPublisherAware {
    private final Logger logger = LoggerFactory.getLogger(RoutingInfrastructureImpl.class);
    private final RouteDefinitionWriter routeDefinitionWriter;
    private final RouteDefinitionRepository routeDefinitionRepository;
    private final RouteDefinitionMapper routeDefinitionMapper;
    private ApplicationEventPublisher applicationEventPublisher;

    public RoutingInfrastructureImpl(RouteDefinitionWriter routeDefinitionWriter, RouteDefinitionRepository routeDefinitionRepository, RouteDefinitionMapper routeDefinitionMapper) {
        this.routeDefinitionWriter = routeDefinitionWriter;
        this.routeDefinitionRepository = routeDefinitionRepository;
        this.routeDefinitionMapper = routeDefinitionMapper;
    }

    @Override
    public void setApplicationEventPublisher(@NonNull ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    /*
     *
     * Spring Cloud Gateway routing gets initiated here
     *
     */
    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .build();
    }


    @Override
    public Mono<?> updateRoutes(Collection<Route> routes) {

        return Mono.zip(
                routes.stream().map(route ->
                                routeDefinitionWriter.save(Mono.create(sink ->
                                        sink.success(routeDefinitionMapper.toRouteDefinition(route)))
                                ))
                        .toList(),
                objects -> objects)
                .doOnSuccess(_ -> {
                    logger.info("Refreshing routes");
                    applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
                })
                .doOnError(throwable -> logger.info("error: "+throwable.getMessage()));
    }

    @Override
    public Flux<Route> getRoutes() {
        return routeDefinitionRepository.getRouteDefinitions().map(routeDefinitionMapper::fromRouteDefinition);
    }
}
