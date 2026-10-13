package hamedmoeeni.api_time_travel.infrastructure.persistence.recording;

import hamedmoeeni.api_time_travel.adapter.persistence.recording.port.RecordingRepository;
import hamedmoeeni.api_time_travel.domain.recording.Recording;
import hamedmoeeni.api_time_travel.usecase.recording.RecordingSearch;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static hamedmoeeni.api_time_travel.infrastructure.persistence.RedisUtils.buildRedisStringTemplate;
import static hamedmoeeni.api_time_travel.infrastructure.persistence.RedisUtils.deserializeSearchResult;

@Repository
@ConditionalOnProperty(
        value = "att.persistence-type",
        havingValue = "REDIS",
        matchIfMissing = true //redis is default persistence repository
)
class RecordingRepositoryRedisImpl implements RecordingRepository {
    private static final String ENTITY_NAME = "RecordingEntity";
    private static final String INDEX_NAME = ENTITY_NAME + "Idx";
    private static final String PREFIX = ENTITY_NAME + ":";
    private final Logger logger = LoggerFactory.getLogger(RecordingRepositoryRedisImpl.class);
    private final RecordingEntityMapper recordingEntityMapper;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final JacksonJsonRedisSerializer<RecordingEntity> serializer = new JacksonJsonRedisSerializer<>(RecordingEntity.class);
    private final JacksonJsonRedisSerializer<RecordingEntityRequestDetails> requestDetailsSerializer = new JacksonJsonRedisSerializer<>(RecordingEntityRequestDetails.class);
    private final JacksonJsonRedisSerializer<RecordingEntityResponseDetails> responseDetailsSerializer = new JacksonJsonRedisSerializer<>(RecordingEntityResponseDetails.class);

    public RecordingRepositoryRedisImpl(RecordingEntityMapper recordingEntityMapper, ReactiveRedisConnectionFactory connectionFactory) {
        this.recordingEntityMapper = recordingEntityMapper;
        this.redisTemplate = buildRedisStringTemplate(connectionFactory);
    }

    @PostConstruct
    public void init() {
        createIndex();
    }

    private void createIndex() {
        redisTemplate.execute(
                        RedisScript.of("redis.call(" +
                                "'FT.create', KEYS[1], 'ON', 'JSON', 'PREFIX', 1, KEYS[2], 'SCHEMA'" +
                                ", '$.requestId', 'AS', 'requestId', 'TEXT'" +
                                ", '$.routeId', 'AS', 'routeId', 'TEXT'" +
                                ", '$.method', 'AS', 'method', 'TEXT'" +
                                ", '$.responseStatus', 'AS', 'responseStatus', 'TEXT'" +
                                ")"),
                        List.of(INDEX_NAME, PREFIX)
                )
                .onErrorComplete(throwable -> {
                    if (throwable.getCause().getMessage().startsWith("Index already exists")) {
                        logger.info("Index already exists");
                        return true;
                    } else {
                        logger.error("Creating index failed", throwable);
                        return false;
                    }
                })
                .collectList()
                .subscribe();
    }

    @Override
    public Mono<Boolean> upsertIgnoreResponse(Recording recording) {
        String json = new String(requestDetailsSerializer.serialize(recordingEntityMapper.toRequestDetails(recording)));
        return redisTemplate.execute(
                        RedisScript.of("redis.call('JSON.merge', KEYS[1], KEYS[2], KEYS[3])"),
                        List.of(PREFIX + recording.getRequestId(), "$", json))
                .collectList()
                .map(_ -> true);
    }

    @Override
    public Mono<Boolean> upsertIgnoreRequest(Recording recording) {
        String json = new String(responseDetailsSerializer.serialize(recordingEntityMapper.toResponseDetails(recording)));
        return redisTemplate.execute(
                        RedisScript.of("redis.call('JSON.merge', KEYS[1], KEYS[2], KEYS[3])"),
                        List.of(PREFIX + recording.getRequestId(), "$", json))
                .collectList()
                .map(_ -> true);
    }

    @Override
    public Flux<Recording> search(RecordingSearch.RecordingSearchCriteria criteria) {
        String query = recordingEntityMapper.criteriaToRedisQuery(criteria);
        Map.Entry<String, String> sort = recordingEntityMapper.criteriaToRedisSort(criteria);
        logger.info("search, query: \"{}\", sort: \"{}\"", query, sort);
        return redisTemplate.execute(
                        RedisScript.of("return redis.call('FT.search', KEYS[1], KEYS[2], 'SORTBY', KEYS[3], KEYS[4], 'LIMIT', KEYS[5], KEYS[6])", List.class),
                        List.of(INDEX_NAME, query, sort.getKey(), sort.getValue(),
                                Long.toString(criteria.getPageStart()), Long.toString(criteria.getPageSize()))
                )
                .flatMap(r -> Flux.fromIterable(deserializeSearchResult(r, serializer)))
                .map(recordingEntityMapper::fromRecordingEntity);
    }
}
