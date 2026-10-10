package hamedmoeeni.api_time_travel.infrastructure.gateway;

import hamedmoeeni.api_time_travel.adapter.persistence.routing.port.RoutingInfrastructure;
import hamedmoeeni.api_time_travel.domain.routing.Route;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
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
import java.util.List;
import java.util.UUID;

@Component
class RoutingInfrastructureImpl implements RoutingInfrastructure, ApplicationEventPublisherAware {
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
    public Mono<Boolean> saveRoutes(Collection<Route> routes) {

        List<Mono<Void>> list = routes.stream().map(route -> {
                            RouteDefinition routeDefinition = routeDefinitionMapper.toRouteDefinition(route);
                            if (routeDefinition.getId() == null) {
                                routeDefinition.setId(UUID.randomUUID().toString());
                            }
                            return routeDefinitionWriter.save(Mono.just(routeDefinition));
                        }
                )
                .toList();

        return Mono.zip(list, _ -> true)
                .doOnSuccess(_ -> {
                    logger.info("Refreshing routes after saving");
                    applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
                })
                .doOnError(throwable -> logger.error("Error refreshing routes after saving", throwable));
    }

    @Override
    public Flux<Route> getRoutes() {
        return routeDefinitionRepository.getRouteDefinitions().map(routeDefinitionMapper::fromRouteDefinition);
    }

    @Override
    public Mono<Boolean> removeRoutes(Collection<String> routeIds) {
        return Mono.zip(
                        routeIds.stream()
                                .map(routeId -> routeDefinitionWriter.delete(Mono.just(routeId))).toList(),
                        _ -> true)
                .doOnSuccess(_ -> {
                    logger.info("Refreshing routes after removing");
                    applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
                })
                .doOnError(throwable -> logger.error("Error refreshing routes after removing", throwable));
    }
}
