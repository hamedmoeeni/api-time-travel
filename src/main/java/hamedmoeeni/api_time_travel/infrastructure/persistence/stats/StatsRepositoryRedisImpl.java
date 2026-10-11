package hamedmoeeni.api_time_travel.infrastructure.persistence.stats;

import hamedmoeeni.api_time_travel.adapter.persistence.stats.port.StatsRepository;
import hamedmoeeni.api_time_travel.domain.stats.Stats;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveHashOperations;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import static hamedmoeeni.api_time_travel.infrastructure.persistence.RedisUtils.buildRedisTemplate;

@Repository
@ConditionalOnProperty(
        value = "att.persistence-type",
        havingValue = "REDIS",
        matchIfMissing = true //redis is default persistence repository
)
class StatsRepositoryRedisImpl implements StatsRepository {
    private static final String STATS_ENTITY_NAME = "StatsEntity";
    private final StatsEntityMapper statsEntityMapper;
    private final ReactiveHashOperations<String, String, StatsEntity> reactiveHashOperations;

    public StatsRepositoryRedisImpl(StatsEntityMapper statsEntityMapper, ReactiveRedisConnectionFactory connectionFactory) {
        this.statsEntityMapper = statsEntityMapper;
        this.reactiveHashOperations = buildRedisTemplate(connectionFactory, StatsEntity.class).opsForHash();
    }

    @Override
    public Mono<Boolean> upsertIgnoreResponse(Stats stats) {
        StatsEntity newEntity = statsEntityMapper.toStatsEntity(stats);
        return reactiveHashOperations.get(STATS_ENTITY_NAME, stats.getRequestId())
                .flatMap(exitingEntity -> {
                    statsEntityMapper.updateRequestDetails(exitingEntity, newEntity);
                    return reactiveHashOperations.put(STATS_ENTITY_NAME, stats.getRequestId(), exitingEntity);
                })
                .switchIfEmpty(reactiveHashOperations.putIfAbsent(STATS_ENTITY_NAME, stats.getRequestId(), newEntity));
    }

    @Override
    public Mono<Boolean> upsertIgnoreRequest(Stats stats) {
        StatsEntity newEntity = statsEntityMapper.toStatsEntity(stats);
        return reactiveHashOperations.get(STATS_ENTITY_NAME, stats.getRequestId())
                .flatMap(exitingEntity -> {
                    statsEntityMapper.updateResponseDetails(exitingEntity, newEntity);
                    return reactiveHashOperations.put(STATS_ENTITY_NAME, stats.getRequestId(), exitingEntity);
                })
                .switchIfEmpty(reactiveHashOperations.putIfAbsent(STATS_ENTITY_NAME, stats.getRequestId(), newEntity));
    }
}
