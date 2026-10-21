package hamedmoeeni.api_time_travel.infrastructure.persistence.recording;

import hamedmoeeni.api_time_travel.domain.recording.Recording;
import hamedmoeeni.api_time_travel.usecase.recording.RecordingStatsService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

@Profile("infrastructure-test")
@SpringBootTest
public class RecordingTimeSeriesRepositoryRedisITest {
    @Autowired
    private RecordingTimeSeriesRepositoryRedisImpl recordingTimeSeriesRepositoryRedis;
    @Autowired
    private ReactiveRedisConnectionFactory redisConnectionFactory;

    @BeforeEach
    public void setUp() {
        redisConnectionFactory.getReactiveConnection().serverCommands().flushAll().block();
    }

    @Order(1)
    @Test
    public void saveRequest() {
        Recording recording1 = new Recording()
                .setRequestId("requestId-1")
                .setRouteId("routeId-1")
                .setUrl("http://anyhost/path1")
                .setMethod("method-1")
                .setTimestamp(ZonedDateTime.ofInstant(Instant.ofEpochMilli(ZonedDateTime.now().toInstant().toEpochMilli()), ZoneOffset.UTC));

        Recording recording2 = new Recording()
                .setRequestId("requestId-2")
                .setRouteId(recording1.getRouteId())
                .setUrl(recording1.getUrl())
                .setMethod(recording1.getMethod())
                .setTimestamp(recording1.getTimestamp());

        Recording recording3 = new Recording()
                .setRequestId("requestId-3")
                .setRouteId(recording1.getRouteId())
                .setUrl(recording1.getUrl())
                .setMethod(recording1.getMethod())
                .setTimestamp(recording1.getTimestamp().plusSeconds(1));

        Recording recording4 = new Recording()
                .setRequestId("requestId-3")
                .setRouteId(recording1.getRouteId())
                .setUrl(recording1.getUrl())
                .setMethod("method-2")
                .setTimestamp(recording1.getTimestamp().plusSeconds(1));

        recordingTimeSeriesRepositoryRedis.saveRecordingIgnoreResponse(recording1).block();
        recordingTimeSeriesRepositoryRedis.saveRecordingIgnoreResponse(recording2).block();
        recordingTimeSeriesRepositoryRedis.saveRecordingIgnoreResponse(recording3).block();
        recordingTimeSeriesRepositoryRedis.saveRecordingIgnoreResponse(recording4).block();

        RecordingStatsService.QueryLabels queryLabels = recordingTimeSeriesRepositoryRedis.getQueryLabels().block();

        assertThat(queryLabels, Matchers.notNullValue());
        assertThat(queryLabels.getMethods(), Matchers.contains("method-1", "method-2"));
        assertThat(queryLabels.getUrls(), Matchers.contains("http://anyhost/path1"));
        assertThat(queryLabels.getRouteIds(), Matchers.contains("routeId-1"));
        assertThat(queryLabels.getStatus(), Matchers.empty());
    }

    @Order(2)
    @Test
    public void saveResponse() {
        Recording recording1 = new Recording()
                .setRequestId("requestId-1")
                .setRouteId("routeId-1")
                .setUrl("http://anyhost/path1")
                .setMethod("method-1")
                .setResponseStatus("status-1")
                .setDuration(5L)
                .setTimestamp(ZonedDateTime.ofInstant(Instant.ofEpochMilli(ZonedDateTime.now().toInstant().toEpochMilli()), ZoneOffset.UTC));

        Recording recording2 = new Recording()
                .setRequestId("requestId-2")
                .setRouteId(recording1.getRouteId())
                .setUrl(recording1.getUrl())
                .setMethod(recording1.getMethod())
                .setResponseStatus(recording1.getResponseStatus())
                .setDuration(6L)
                .setTimestamp(recording1.getTimestamp());

        Recording recording3 = new Recording()
                .setRequestId("requestId-3")
                .setRouteId(recording1.getRouteId())
                .setUrl(recording1.getUrl())
                .setMethod(recording1.getMethod())
                .setResponseStatus(recording2.getResponseStatus())
                .setDuration(7L)
                .setTimestamp(recording1.getTimestamp().plusSeconds(1));

        recordingTimeSeriesRepositoryRedis.saveRecordingIgnoreRqquest(recording1).block();
        recordingTimeSeriesRepositoryRedis.saveRecordingIgnoreRqquest(recording2).block();
        recordingTimeSeriesRepositoryRedis.saveRecordingIgnoreRqquest(recording3).block();

        RecordingStatsService.QueryLabels queryLabels = recordingTimeSeriesRepositoryRedis.getQueryLabels().block();

        assertThat(queryLabels, Matchers.notNullValue());
        assertThat(queryLabels.getMethods(), Matchers.contains("method-1"));
        assertThat(queryLabels.getUrls(), Matchers.contains("http://anyhost/path1"));
        assertThat(queryLabels.getRouteIds(), Matchers.contains("routeId-1"));
        assertThat(queryLabels.getStatus(), Matchers.contains("status-1"));
    }

    @Order(3)
    @Test
    public void queryCounts() {
        Recording recording1 = new Recording()
                .setRequestId("requestId-1")
                .setRouteId("routeId-1")
                .setUrl("http://anyhost/path1")
                .setMethod("get")
                .setTimestamp(ZonedDateTime.ofInstant(Instant.ofEpochMilli(ZonedDateTime.now().toInstant().toEpochMilli()), ZoneOffset.UTC));

        Recording recording2 = new Recording()
                .setRequestId("requestId-2")
                .setRouteId(recording1.getRouteId())
                .setUrl(recording1.getUrl())
                .setMethod("post")
                .setTimestamp(recording1.getTimestamp().plusSeconds(1));

        Recording recording3 = new Recording()
                .setRequestId("requestId-3")
                .setRouteId("routeId-2")
                .setUrl("http://anyhost/path2")
                .setMethod("get")
                .setTimestamp(recording1.getTimestamp());

        Recording recording4 = new Recording()
                .setRequestId("requestId-3")
                .setRouteId(recording3.getRouteId())
                .setUrl(recording3.getUrl())
                .setMethod("post")
                .setTimestamp(recording1.getTimestamp().plusSeconds(1));

        recordingTimeSeriesRepositoryRedis.saveRecordingIgnoreResponse(recording1).block();
        recordingTimeSeriesRepositoryRedis.saveRecordingIgnoreResponse(recording2).block();
        recordingTimeSeriesRepositoryRedis.saveRecordingIgnoreResponse(recording3).block();
        recordingTimeSeriesRepositoryRedis.saveRecordingIgnoreResponse(recording4).block();

        List<RecordingStatsService.StatsTimeSeries> queryResult = recordingTimeSeriesRepositoryRedis.query(
                new RecordingStatsService.StatsQuery().setMethods(List.of("get"))
        ).block();

        assertThat(queryResult, Matchers.hasSize(1));
        assertThat(queryResult.getFirst().getLabel(), Matchers.equalTo("get"));
        assertThat(queryResult.getFirst().getTime(), Matchers.equalTo(new Long[]{recording1.getTimestamp().toInstant().toEpochMilli()}));
        assertThat(queryResult.getFirst().getValue(), Matchers.equalTo(new Double[]{2.0}));

        queryResult = recordingTimeSeriesRepositoryRedis.query(
                new RecordingStatsService.StatsQuery()
                        .setMethods(List.of("get", "post"))
                        .setRouteIds(List.of("routeId-1", "routeId-2"))
        ).block();

        assertThat(queryResult, Matchers.hasSize(2));
        assertThat(queryResult.getFirst().getLabel(), Matchers.equalTo("get"));
        assertThat(queryResult.getFirst().getTime(), Matchers.equalTo(new Long[]{recording1.getTimestamp().toInstant().toEpochMilli()}));
        assertThat(queryResult.getFirst().getValue(), Matchers.equalTo(new Double[]{2.0}));
        assertThat(queryResult.get(1).getLabel(), Matchers.equalTo("post"));
        assertThat(queryResult.get(1).getTime(), Matchers.equalTo(new Long[]{recording2.getTimestamp().toInstant().toEpochMilli()}));
        assertThat(queryResult.get(1).getValue(), Matchers.equalTo(new Double[]{2.0}));

        queryResult = recordingTimeSeriesRepositoryRedis.query(
                new RecordingStatsService.StatsQuery()
                        .setMethods(List.of("get", "post"))
                        .setRouteIds(List.of("routeId-1", "routeId-2"))
                        .setUrls(List.of("http://anyhost/path1", "http://anyhost/path2"))
        ).block();

        assertThat(queryResult, Matchers.hasSize(2));
        assertThat(queryResult.getFirst().getLabel(), Matchers.equalTo("get"));
        assertThat(queryResult.getFirst().getTime(), Matchers.equalTo(new Long[]{recording1.getTimestamp().toInstant().toEpochMilli()}));
        assertThat(queryResult.getFirst().getValue(), Matchers.equalTo(new Double[]{2.0}));
        assertThat(queryResult.get(1).getLabel(), Matchers.equalTo("post"));
        assertThat(queryResult.get(1).getTime(), Matchers.equalTo(new Long[]{recording2.getTimestamp().toInstant().toEpochMilli()}));
        assertThat(queryResult.get(1).getValue(), Matchers.equalTo(new Double[]{2.0}));
    }

    @Order(4)
    @Test
    public void queryAvg() {
        Recording recording1 = new Recording()
                .setRequestId("requestId-1")
                .setRouteId("routeId-1")
                .setUrl("http://anyhost/path1")
                .setMethod("get")
                .setResponseStatus("200")
                .setDuration(10L)
                .setTimestamp(ZonedDateTime.ofInstant(Instant.ofEpochMilli(ZonedDateTime.now().toInstant().toEpochMilli()), ZoneOffset.UTC));

        Recording recording2 = new Recording()
                .setRequestId("requestId-2")
                .setRouteId(recording1.getRouteId())
                .setUrl(recording1.getUrl())
                .setMethod("post")
                .setResponseStatus("200")
                .setDuration(20L)
                .setTimestamp(recording1.getTimestamp().plusSeconds(1));

        Recording recording3 = new Recording()
                .setRequestId("requestId-3")
                .setRouteId("routeId-2")
                .setUrl("http://anyhost/path2")
                .setMethod("get")
                .setResponseStatus("301")
                .setDuration(1L)
                .setTimestamp(recording1.getTimestamp());

        Recording recording4 = new Recording()
                .setRequestId("requestId-3")
                .setRouteId(recording3.getRouteId())
                .setUrl(recording3.getUrl())
                .setMethod("post")
                .setResponseStatus("301")
                .setDuration(2L)
                .setTimestamp(recording1.getTimestamp().plusSeconds(1));

        recordingTimeSeriesRepositoryRedis.saveRecordingIgnoreRqquest(recording1).block();
        recordingTimeSeriesRepositoryRedis.saveRecordingIgnoreRqquest(recording2).block();
        recordingTimeSeriesRepositoryRedis.saveRecordingIgnoreRqquest(recording3).block();
        recordingTimeSeriesRepositoryRedis.saveRecordingIgnoreRqquest(recording4).block();

        List<RecordingStatsService.StatsTimeSeries> queryResult = recordingTimeSeriesRepositoryRedis.query(
                new RecordingStatsService.StatsQuery()
                        .setOutputType(RecordingStatsService.StatsQuery.OutputType.AVG_RESPONSE_TIME)
                        .setGroupBy("method")
        ).block();

        assertThat(queryResult, Matchers.hasSize(2));
        assertThat(queryResult.getFirst().getLabel(), Matchers.equalTo("get"));
        assertThat(queryResult.getFirst().getTime(), Matchers.equalTo(new Long[]{recording1.getTimestamp().toInstant().toEpochMilli()}));
        assertThat(queryResult.getFirst().getValue(), Matchers.equalTo(new Double[]{5.5}));
        assertThat(queryResult.get(1).getLabel(), Matchers.equalTo("post"));
        assertThat(queryResult.get(1).getTime(), Matchers.equalTo(new Long[]{recording2.getTimestamp().toInstant().toEpochMilli()}));
        assertThat(queryResult.get(1).getValue(), Matchers.equalTo(new Double[]{11.0}));

        queryResult = recordingTimeSeriesRepositoryRedis.query(
                new RecordingStatsService.StatsQuery()
                        .setOutputType(RecordingStatsService.StatsQuery.OutputType.AVG_RESPONSE_TIME)
                        .setMethods(List.of("get,post"))
                        .setGroupBy("status")
        ).block();

        assertThat(queryResult, Matchers.hasSize(2));
        assertThat(queryResult.getFirst().getLabel(), Matchers.equalTo("200"));
        assertThat(queryResult.getFirst().getTime(), Matchers.equalTo(new Long[]{
                recording1.getTimestamp().toInstant().toEpochMilli(),
                recording2.getTimestamp().toInstant().toEpochMilli()}));
        assertThat(queryResult.getFirst().getValue(), Matchers.equalTo(new Double[]{10.0, 20.0}));
        assertThat(queryResult.get(1).getLabel(), Matchers.equalTo("301"));
        assertThat(queryResult.get(1).getTime(), Matchers.equalTo(new Long[]{
                recording3.getTimestamp().toInstant().toEpochMilli(),
                recording4.getTimestamp().toInstant().toEpochMilli()}));
        assertThat(queryResult.get(1).getValue(), Matchers.equalTo(new Double[]{1.0, 2.0}));

    }
}
