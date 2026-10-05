package hamedmoeeni.api_time_travel.adapter.persistence.routing;

import hamedmoeeni.api_time_travel.usecase.routing.Route;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@ConditionalOnProperty(
        value = "att.persistence-type",
        havingValue = "REDIS",
        matchIfMissing = true //redis is default persistence repository
)
public class RouteRepositoryRedisImpl implements RouteRepository {
    private static final String ROUTE_ENTITY_NAME = "RouteEntity";
    private final RouteEntityMapper routeEntityMapper;
    private final ReactiveHashOperations<String, String, RouteEntity> redisOperations;

    public RouteRepositoryRedisImpl(RouteEntityMapper routeEntityMapper, ReactiveRedisConnectionFactory connectionFactory) {
        this.routeEntityMapper = routeEntityMapper;
        this.redisOperations = buildRedisTemplate(connectionFactory).opsForHash();
    }

    private ReactiveRedisTemplate<String, RouteEntity> buildRedisTemplate(ReactiveRedisConnectionFactory connectionFactory) {
        RedisSerializer<String> keySerializer = new StringRedisSerializer();
        RedisSerializer<RouteEntity> valueSerializer = new JacksonJsonRedisSerializer<>(RouteEntity.class);

        RedisSerializationContext.RedisSerializationContextBuilder<String, RouteEntity> builder = RedisSerializationContext.newSerializationContext();
        RedisSerializationContext<String, RouteEntity> serializationContext = builder
                .key(keySerializer)
                .value(valueSerializer)
                .hashKey(keySerializer)
                .hashValue(valueSerializer)
                .build();

        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }

    @Override
    public Flux<Route> findAllRoutes() {
        return redisOperations.entries(ROUTE_ENTITY_NAME)
                .map(e -> routeEntityMapper.fromRouteEntity(e.getValue()));
    }

    @Override
    public Mono<Boolean> saveRoutes(Collection<Route> routes) {
        return redisOperations.putAll(ROUTE_ENTITY_NAME, routes.stream().map(route ->
                        route.getId() == null ? route.setId(UUID.randomUUID().toString()) : route
                )
                .collect(Collectors.toMap(
                        route -> route.getId() == null ? UUID.randomUUID().toString() : route.getId(),
                        routeEntityMapper::toRouteEntity)
                )
        );
    }

    @Override
    public Mono<Boolean> deleteById(Collection<String> routeIds) {
        return redisOperations.remove(ROUTE_ENTITY_NAME, routeIds.toArray())
                .map(_ -> true);
    }
}
