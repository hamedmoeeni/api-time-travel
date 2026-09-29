package hamedmoeeni.api_time_travel.adapter.rest;

import hamedmoeeni.api_time_travel.usecase.routing.RoutingManager;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${att.management.uri-path}")
public class RoutingController {
    private final RoutingManager routingManager;
    private final RouteDtoMapper routeDtoMapper;

    public RoutingController(RoutingManager routingManager,
                             RouteDtoMapper routeDtoMapper) {
        this.routingManager = routingManager;
        this.routeDtoMapper = routeDtoMapper;
    }

    @PostMapping("/routes")
    public Mono<Object> saveRoutes(@RequestBody LinkedList<RouteDto> routes) {
        return routingManager.updateRoutes(
                        routes.stream().map(routeDtoMapper::fromDto).collect(Collectors.toList())
                )
                .map(_ -> new Object());
    }

    @GetMapping("/routes")
    public Mono<Collection<RouteDto>> getRoutes() {
        return routingManager.getRoutes()
                .map(routeDtoMapper::toDto)
                .collect(Collectors.toList());
    }
}
