package hamedmoeeni.api_time_travel.infrastructure.persistence.recording;

import hamedmoeeni.api_time_travel.adapter.persistence.recording.port.RecordingTimeSeriesRepository;
import hamedmoeeni.api_time_travel.domain.recording.Recording;
import hamedmoeeni.api_time_travel.infrastructure.config.ATTConfig;
import hamedmoeeni.api_time_travel.usecase.recording.RecordingStatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static hamedmoeeni.api_time_travel.infrastructure.persistence.RedisUtils.buildRedisStringTemplate;

@Repository
@ConditionalOnProperty(
        value = "att.persistence-type",
        havingValue = "REDIS",
        matchIfMissing = true //redis is default persistence repository
)
public class RecordingTimeSeriesRepositoryRedisImpl implements RecordingTimeSeriesRepository {

    private final Logger logger = LoggerFactory.getLogger(RecordingTimeSeriesRepositoryRedisImpl.class);
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final Set<String> createdTimeSeries = ConcurrentHashMap.newKeySet();
    private final ATTConfig attConfig;

    public RecordingTimeSeriesRepositoryRedisImpl(ATTConfig attConfig, ReactiveRedisConnectionFactory connectionFactory) {
        this.attConfig = attConfig;
        this.redisTemplate = buildRedisStringTemplate(connectionFactory);
    }

    @Override
    public Mono<Boolean> saveRecordingIgnoreResponse(Recording recording) {
        String timeSeriesName = requestTimeSeriesName(recording);
        return createRequestTimeSeries(timeSeriesName, recording)
                .flatMap(result -> {
                    if (result) {
                        return addToRequestTimeSeries(timeSeriesName, recording);
                    } else {
                        return Mono.just(false);
                    }
                });
    }

    @Override
    public Mono<Boolean> saveRecordingIgnoreRqquest(Recording recording) {
        String timeSeriesName = responseTimeSeriesName(recording);
        Iterable<? extends Mono<?>> monos = Arrays.stream(ScalarType.values())
                .map(scalarType -> createResponseTimeSeries(timeSeriesName, scalarType, recording)).toList();
        return Mono.zip(monos, _ -> true)
                .then(addToResponseTimeSeries(timeSeriesName, recording));
    }

    private String requestTimeSeriesName(Recording recording) {
        return String.format("%s:req:%s:%s:%s", attConfig.getPersistenceEntitiesPrefix(), recording.getMethod(), escapeUrl(recording.getUrl()), recording.getRouteId());
    }

    private String responseTimeSeriesName(Recording recording) {
        return String.format("%s:resp:%s:%s:%s:%s", attConfig.getPersistenceEntitiesPrefix(), recording.getMethod(), escapeUrl(recording.getUrl()), recording.getResponseStatus(), recording.getRouteId());
    }

    private String escapeUrl(String url) {
        return url == null ? null : url.replace(":", "%3A");
    }

    private String unEscapeUrl(String url) {
        return url == null ? null : url.replace("%3A", ":");
    }

    private Mono<Boolean> createRequestTimeSeries(String timeSeriesName, Recording recording) {

        if (!createdTimeSeries.contains(timeSeriesName)) {
            return redisTemplate.execute(RedisScript.of("redis.call(" +
                                    "'TS.CREATE', KEYS[1]" +
                                    ", 'DUPLICATE_POLICY', 'SUM'" +
                                    ", 'labels', 'type', 'request', 'method', KEYS[2], 'url', KEYS[3], 'routeId', KEYS[4]" +
                                    ",  'scalarType', KEYS[5]" +
                                    ")"),
                            List.of(timeSeriesName, recording.getMethod(), escapeUrl(recording.getUrl()), recording.getRouteId(),
                                    ScalarType.COUNT.name()
                                    ))
                    .collectList()
                    .map(_ -> {
                        createdTimeSeries.add(timeSeriesName);
                        return true;
                    })
                    .onErrorComplete(throwable -> {
                        if (throwable.getCause().getMessage().startsWith("ERR TSDB: key already exists")) {
                            createdTimeSeries.add(timeSeriesName);
                            return true;
                        } else {
                            logger.error("Creating time series failed", throwable);
                            return false;
                        }
                    });
        }
        return Mono.just(true);
    }

    private Mono<Boolean> createResponseTimeSeries(String timeSeriesName, ScalarType scalarType, Recording recording) {
        String tsName = timeSeriesName + ":" + scalarType.name();
        if (!createdTimeSeries.contains(tsName)) {
            return redisTemplate.execute(RedisScript.of("redis.call(" +
                                    "'TS.CREATE', KEYS[1]" +
                                    ", 'DUPLICATE_POLICY', 'SUM'" +
                                    ", 'labels', 'type', 'response', 'method', KEYS[2], 'url', KEYS[3], 'status', KEYS[4]" +
                                    ", 'routeId', KEYS[5], 'scalarType', KEYS[6]" +
                                    ")"),
                            List.of(tsName,
                                    recording.getMethod(), escapeUrl(recording.getUrl()), String.valueOf(recording.getResponseStatus()),
                                    recording.getRouteId(), scalarType.name()
                            ))
                    .collectList()
                    .map(_ -> {
                        createdTimeSeries.add(tsName);
                        return true;
                    })
                    .onErrorComplete(throwable -> {
                        if (throwable.getCause().getMessage().startsWith("ERR TSDB: key already exists")) {
                            createdTimeSeries.add(timeSeriesName);
                            return true;
                        } else {
                            logger.error("Creating time series failed", throwable);
                            return false;
                        }
                    });
        }
        return Mono.just(true);
    }

    private Mono<Boolean> addToRequestTimeSeries(String timeSeriesName, Recording recording) {
        return redisTemplate.execute(RedisScript.of("redis.call(" +
                                "'TS.ADD', KEYS[1], KEYS[2], 1 " +
                                ")"),
                        List.of(timeSeriesName, String.valueOf(recording.getTimestamp().toInstant().toEpochMilli())))
                .collectList()
                .then(Mono.just(true));
    }

    private Mono<Boolean> addToResponseTimeSeries(String timeSeriesName, Recording recording) {
        String countTimeSeriesName = timeSeriesName + ":" + ScalarType.COUNT.name();
        String durationTimeSeriesName = timeSeriesName + ":" + ScalarType.DURATION.name();
        String timestamp = String.valueOf(recording.getTimestamp().toInstant().toEpochMilli());

        return redisTemplate.execute(RedisScript.of("redis.call(" +
                                "'TS.MADD', KEYS[1], KEYS[2], 1, KEYS[3], KEYS[2], KEYS[4]" +
                                ")"),
                        List.of(
                                countTimeSeriesName, timestamp,
                                durationTimeSeriesName, String.valueOf(recording.getDuration())
                        ))
                .collectList()
                .then(Mono.just(true));
    }

    @Override
    public Mono<RecordingStatsService.QueryLabels> getQueryLabels() {
        RecordingStatsService.QueryLabels queryLabels = new RecordingStatsService.QueryLabels();
        return Mono.zip(
                        redisTemplate.execute(RedisScript.of("return redis.call('TS.QUERYLABELS', 'values', 'method')", List.class))
                                .collectList()
                                .map(results -> queryLabels.setMethods(convertQueryLabelResult(results))),
                        redisTemplate.execute(RedisScript.of("return redis.call('TS.QUERYLABELS', 'values', 'url')", List.class))
                                .collectList()
                                .map(results -> queryLabels.setUrls(
                                        convertQueryLabelResult(results).stream()
                                                .map(r -> r.replace("%3A", ":"))
                                                .collect(Collectors.toList()))
                                ),
                        redisTemplate.execute(RedisScript.of("return redis.call('TS.QUERYLABELS', 'values', 'status')", List.class))
                                .collectList()
                                .map(results -> queryLabels.setStatus(convertQueryLabelResult(results))),
                        redisTemplate.execute(RedisScript.of("return redis.call('TS.QUERYLABELS', 'values', 'routeId')", List.class))
                                .collectList()
                                .map(results -> queryLabels.setRouteIds(convertQueryLabelResult(results)))
                )
                .map(_ -> queryLabels);
    }

    private List<String> convertQueryLabelResult(@SuppressWarnings("rawtypes") List<List> results) {
        if (results != null && !results.isEmpty()) {
            return ((List<?>) results.getFirst()).stream().filter(r -> r instanceof String).map(r -> (String) r).toList();
        }
        return List.of();
    }


    @Override
    public Mono<List<RecordingStatsService.StatsTimeSeries>> query(RecordingStatsService.StatsQuery statsQuery) {
        if (statsQuery.getOutputType() == RecordingStatsService.StatsQuery.OutputType.AVG_RESPONSE_TIME) {
            return queryWithAggregation(statsQuery, "response", ScalarType.DURATION)
                    .flatMap(sumResult ->
                            queryWithAggregation(statsQuery, "response", ScalarType.COUNT)
                                    .map(countResult -> calculateAvg(sumResult, countResult))
                    );
        } else if ("status".equals(statsQuery.getGroupBy()) && statsQuery.getOutputType() == RecordingStatsService.StatsQuery.OutputType.COUNT) {
            return queryWithAggregation(statsQuery, "response", ScalarType.COUNT);
        }
        return queryWithAggregation(statsQuery, "request", ScalarType.COUNT);
    }

    private List<RecordingStatsService.StatsTimeSeries> calculateAvg(List<RecordingStatsService.StatsTimeSeries> sumResult, List<RecordingStatsService.StatsTimeSeries> countResult) {
        Map<String, Map<Long, Double>> sumMap = timeSeriesToMap(sumResult);
        Map<String, Map<Long, Double>> countMap = timeSeriesToMap(countResult);
        List<RecordingStatsService.StatsTimeSeries> r = new LinkedList<>();
        countMap.forEach((label, countTs) -> {
            RecordingStatsService.StatsTimeSeries rs = new RecordingStatsService.StatsTimeSeries()
                    .setLabel(label)
                    .setTime(new Long[countTs.size()])
                    .setValue(new Double[countTs.size()]);
            int[] i = {0};
            countTs.forEach((time, count) -> {
                rs.getTime()[i[0]] = time;
                rs.getValue()[i[0]] = sumMap.get(label).get(time) / count;
                i[0]++;
            });

            r.add(rs);
        });

        return r;
    }

    private Map<String, Map<Long, Double>> timeSeriesToMap(List<RecordingStatsService.StatsTimeSeries> sumResult) {
        Map<String, Map<Long, Double>> r = new LinkedHashMap<>();
        for (RecordingStatsService.StatsTimeSeries s : sumResult) {
            Map<Long, Double> m = new LinkedHashMap<>();
            for (int i = 0; i < s.getTime().length; i++) {
                m.put(s.getTime()[i], s.getValue()[i]);
            }
            r.put(s.getLabel(), m);
        }
        return r;
    }

    private Mono<List<RecordingStatsService.StatsTimeSeries>> queryWithAggregation(RecordingStatsService.StatsQuery statsQuery, String type, ScalarType scalarType) {
        String script = "";
        List<String> keys = Stream.of(
                        statsQuery.getFromTimestamp() == null ? "-" : statsQuery.getFromTimestamp().toString(),
                        statsQuery.getToTimestamp() == null ? "+" : statsQuery.getToTimestamp().toString(),
                        "SUM",
                        aggregationBucketDurationString(statsQuery),
                        "scalarType=" + scalarType.name(),
                        "type=" + type,
                        methodFilterString(statsQuery),
                        routeIdFilterString(statsQuery),
                        urlFilterString(statsQuery),
                        statusFilterString(statsQuery),
                        statsQuery.getGroupBy() == null ? "method" : statsQuery.getGroupBy(),
                        "SUM"
                )
                .filter(s -> !s.isBlank())
                .toList();
        switch (keys.size()) {
            case 7 ->
                    script = "return redis.call('TS.MRANGE', KEYS[1], KEYS[2], 'AGGREGATION', KEYS[3], KEYS[4], 'FILTER', KEYS[5], 'GROUPBY', KEYS[6], 'REDUCE', KEYS[7])";
            case 8 ->
                    script = "return redis.call('TS.MRANGE', KEYS[1], KEYS[2], 'AGGREGATION', KEYS[3], KEYS[4], 'FILTER', KEYS[5], KEYS[6], 'GROUPBY', KEYS[7], 'REDUCE', KEYS[8])";
            case 9 ->
                    script = "return redis.call('TS.MRANGE', KEYS[1], KEYS[2], 'AGGREGATION', KEYS[3], KEYS[4], 'FILTER', KEYS[5], KEYS[6], KEYS[7], 'GROUPBY', KEYS[8], 'REDUCE', KEYS[9])";
            case 10 ->
                    script = "return redis.call('TS.MRANGE', KEYS[1], KEYS[2], 'AGGREGATION', KEYS[3], KEYS[4], 'FILTER', KEYS[5], KEYS[6], KEYS[7], KEYS[8], 'GROUPBY', KEYS[9], 'REDUCE', KEYS[10])";
            case 11 ->
                    script = "return redis.call('TS.MRANGE', KEYS[1], KEYS[2], 'AGGREGATION', KEYS[3], KEYS[4], 'FILTER', KEYS[5], KEYS[6], KEYS[7], KEYS[8], KEYS[9], 'GROUPBY', KEYS[10], 'REDUCE', KEYS[11])";
            case 12 ->
                    script = "return redis.call('TS.MRANGE', KEYS[1], KEYS[2], 'AGGREGATION', KEYS[3], KEYS[4], 'FILTER', KEYS[5], KEYS[6], KEYS[7], KEYS[8], KEYS[9], KEYS[10], 'GROUPBY', KEYS[11], 'REDUCE', KEYS[12])";
        }

        return redisTemplate.execute(RedisScript.of(script, List.class), keys)
                .collectList()
                .map(lists -> {
                    List<RecordingStatsService.StatsTimeSeries> result = convertQueryResult(lists);
                    result.forEach(r -> {
                        if (r.getLabel() != null) {
                            if (r.getLabel().startsWith("url=")) {
                                r.setLabel(unEscapeUrl(r.getLabel().replaceFirst("url=", "")));
                            } else {
                                r.setLabel(r.getLabel()
                                        .replaceFirst("type=", "")
                                        .replaceFirst("method=", "")
                                        .replaceFirst("routeId=", "")
                                        .replaceFirst("status=", "")
                                );
                            }
                        }
                    });
                    return result;
                });
    }

    private String aggregationBucketDurationString(RecordingStatsService.StatsQuery ignoredStatsQuery) {
        return String.valueOf(Duration.ofMinutes(1).toMillis());
    }

    private String methodFilterString(RecordingStatsService.StatsQuery statsQuery) {
        if (statsQuery.getMethods() != null && !statsQuery.getMethods().isEmpty()) {
            return "method=(" + String.join(",", statsQuery.getMethods()) + ")";
        }
        return "";
    }

    private String routeIdFilterString(RecordingStatsService.StatsQuery statsQuery) {
        if (statsQuery.getRouteIds() != null && !statsQuery.getRouteIds().isEmpty()) {
            return "routeId=(" + String.join(",", statsQuery.getRouteIds()) + ")";
        }
        return "";
    }

    private String urlFilterString(RecordingStatsService.StatsQuery statsQuery) {
        if (statsQuery.getUrls() != null && !statsQuery.getUrls().isEmpty()) {
            return "url=(" + statsQuery.getUrls().stream().map(this::escapeUrl).collect(Collectors.joining(",")) + ")";
        }
        return "";
    }

    private String statusFilterString(RecordingStatsService.StatsQuery statsQuery) {
        if (statsQuery.getStatus() != null && !statsQuery.getStatus().isEmpty()) {
            return "status=(" + String.join(",", statsQuery.getStatus()) + ")";
        }
        return "";
    }

    private List<RecordingStatsService.StatsTimeSeries> convertQueryResult(List<?> results) {
        List<RecordingStatsService.StatsTimeSeries> result = new LinkedList<>();
        if (results != null) {
            results.forEach(r -> {
                if (r instanceof List) {
                    ((List<?>) r).forEach(rr -> {
                        RecordingStatsService.StatsTimeSeries ts = new RecordingStatsService.StatsTimeSeries();
                        Object first = ((List<?>) rr).getFirst();
                        Object third = ((List<?>) rr).size() >= 3 ? ((List<?>) rr).get(2) : null;
                        if (first instanceof String) {
                            ts.setLabel(first.toString());
                        }
                        if (third instanceof List<?>) {
                            Long[] timestamps = new Long[((List<?>) third).size()];
                            Double[] values = new Double[((List<?>) third).size()];
                            int[] i = new int[]{0};
                            ((List<?>) third).forEach(tv -> {
                                if (tv instanceof List<?> castTv) {
                                    if (!castTv.isEmpty()) {
                                        timestamps[i[0]] = Long.valueOf(castTv.getFirst().toString());
                                    }
                                    if (castTv.size() >= 2) {
                                        values[i[0]] = Double.valueOf(castTv.get(1).toString());
                                    }
                                }
                                i[0]++;
                            });
                            result.add(ts.setTime(timestamps).setValue(values));
                        }
                    });
                }
            });
        }
        return result;
    }
}
