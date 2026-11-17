package hamedmoeeni.api_time_travel.infrastructure.persistence.recording;

import hamedmoeeni.api_time_travel.domain.recording.Recording;
import hamedmoeeni.api_time_travel.usecase.recording.RecordingStatsService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;

import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;

@Profile("infrastructure-test")
@Execution(ExecutionMode.SAME_THREAD)
@SpringBootTest
public class RecordingTimeSeriesRepositoryRedisITest {
    @Autowired
    private RecordingTimeSeriesRepositoryRedisImpl recordingTimeSeriesRepositoryRedis;
    @Autowired
    private ReactiveRedisConnectionFactory redisConnectionFactory;

    @AfterEach
    public void setUp() {
        redisConnectionFactory.getReactiveConnection().serverCommands().flushDb().block();
    }

    private void saveRequest(String requestId, String routeId, String url, String method, String timestamp) {
        Recording recording = new Recording()
                .setRequestId(requestId).setRouteId(routeId).setUrl(url).setMethod(method)
                .setTimestamp(LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).atZone(ZoneOffset.UTC));
        recordingTimeSeriesRepositoryRedis.saveRecordingIgnoreResponse(recording).block();
    }

    private void saveResponse(String requestId, String routeId, String url, String method, String timestamp, Integer httpStatus, Long duration) {
        Recording recording = new Recording()
                .setRequestId(requestId).setRouteId(routeId).setUrl(url).setMethod(method)
                .setTimestamp(LocalDateTime.parse(timestamp, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")).atZone(ZoneOffset.UTC))
                .setResponseStatus(httpStatus).setDuration(duration);
        recordingTimeSeriesRepositoryRedis.saveRecordingIgnoreRqquest(recording).block();
    }

    @Test
    public void queryLabelsFromRequest() {
        saveRequest("requestId-1", "routeId-1", "http://test/path-1", "GET", "2000-01-01 00:00:00");
        saveRequest("requestId-2", "routeId-1", "http://test/path-1", "GET", "2000-01-01 00:00:00");
        saveRequest("requestId-3", "routeId-2", "http://test/path-2", "GET", "2000-01-01 00:00:01");
        saveRequest("requestId-3", "routeId-1", "http://test/path-1", "POST", "2000-01-01 00:00:02");

        RecordingStatsService.QueryLabels queryLabels = recordingTimeSeriesRepositoryRedis.getQueryLabels().block();

        assertThat(queryLabels, Matchers.notNullValue());
        assertThat(queryLabels.getMethods(), Matchers.contains("GET", "POST"));
        assertThat(queryLabels.getUrls(), Matchers.contains("http://test/path-1", "http://test/path-2"));
        assertThat(queryLabels.getRouteIds(), Matchers.contains("routeId-1", "routeId-2"));
        assertThat(queryLabels.getStatus(), Matchers.empty());
    }

    @Test
    public void queryLabelsFromResponse() {
        saveResponse("requestId-1", "routeId-1", "http://test/path-1", "GET", "2000-01-01 00:00:00", 200, 5L);
        saveResponse("requestId-2", "routeId-1", "http://test/path-1", "POST", "2000-01-01 00:00:00", 200, 6L);
        saveResponse("requestId-3", "routeId-1", "http://test/path-2", "GET", "2000-01-01 00:00:01", 301, 7L);

        RecordingStatsService.QueryLabels queryLabels = recordingTimeSeriesRepositoryRedis.getQueryLabels().block();

        assertThat(queryLabels, Matchers.notNullValue());
        assertThat(queryLabels.getMethods(), Matchers.contains("GET", "POST"));
        assertThat(queryLabels.getUrls(), Matchers.contains("http://test/path-1", "http://test/path-2"));
        assertThat(queryLabels.getRouteIds(), Matchers.contains("routeId-1"));
        assertThat(queryLabels.getStatus(), Matchers.contains("200", "301"));
    }

    @Test
    public void queryCounts() {
        /*
         * 1 req/s for 1 minute
         * all have same routeId
         * even seconds have GET method, odd seconds have POST method
         * every 10 second Urls alternate
         * every 30 seconds http status code alternates
         */
        for (int i = 0; i < 60; i++) {
            String timestamp = "2000-01-01 00:00:" + new DecimalFormat("00").format(i);
            String requestId = "requestId-" + i;
            String routeId = "routeId-1";
            String method = i % 2 == 0 ? "GET" : "POST";
            String url = (i / 10) % 2 == 0 ? "http://test/path1" : "http://test/path2";
            int httpStatus = (i / 30) % 2 == 0 ? 200 : 503;
            saveRequest(requestId, routeId, url, method, timestamp);
            saveResponse(requestId, routeId, url, method, timestamp, httpStatus, 5L);
        }

        /*
         * No record for 1 minute, then 1 patch request
         */
        saveRequest("requestId-last", "routeId-1", "http://test/path1", "PATCH", "2000-01-01 00:02:00");
        saveResponse("requestId-last", "routeId-1", "http://test/path1", "PATCH", "2000-01-01 00:02:00", 200, 5L);

        // Group by routeId
        List<RecordingStatsService.StatsTimeSeries> queryResult = recordingTimeSeriesRepositoryRedis.query(
                new RecordingStatsService.StatsQuery()
                        .setOutputType(RecordingStatsService.StatsQuery.OutputType.COUNT)
                        .setGroupBy("routeId")
        ).block();

        assertThat(queryResult, Matchers.hasSize(1));
        assertThat(queryResult.getFirst().getLabel(), Matchers.equalTo("routeId-1"));
        assertThat(queryResult.getFirst().getTime(), Matchers.equalTo(new Long[]{946684800000L, 946684920000L}));
        assertThat(queryResult.getFirst().getValue(), Matchers.equalTo(new Double[]{60.0, 1.0}));

        // Group by method
        queryResult = recordingTimeSeriesRepositoryRedis.query(
                new RecordingStatsService.StatsQuery()
                        .setOutputType(RecordingStatsService.StatsQuery.OutputType.COUNT)
                        .setGroupBy("method")
        ).block();

        assertThat(queryResult, Matchers.hasSize(3));
        assertThat(queryResult.getFirst().getLabel(), Matchers.equalTo("GET"));
        assertThat(queryResult.getFirst().getTime(), Matchers.equalTo(new Long[]{946684800000L}));
        assertThat(queryResult.getFirst().getValue(), Matchers.equalTo(new Double[]{30.0}));
        assertThat(queryResult.get(1).getLabel(), Matchers.equalTo("PATCH"));
        assertThat(queryResult.get(1).getTime(), Matchers.equalTo(new Long[]{946684920000L}));
        assertThat(queryResult.get(1).getValue(), Matchers.equalTo(new Double[]{1.0}));
        assertThat(queryResult.get(2).getLabel(), Matchers.equalTo("POST"));
        assertThat(queryResult.get(2).getTime(), Matchers.equalTo(new Long[]{946684800000L}));
        assertThat(queryResult.get(2).getValue(), Matchers.equalTo(new Double[]{30.0}));


        // Group by url
        queryResult = recordingTimeSeriesRepositoryRedis.query(
                new RecordingStatsService.StatsQuery()
                        .setOutputType(RecordingStatsService.StatsQuery.OutputType.COUNT)
                        .setGroupBy("url")
        ).block();

        assertThat(queryResult, Matchers.hasSize(2));
        assertThat(queryResult.getFirst().getLabel(), Matchers.equalTo("http://test/path1"));
        assertThat(queryResult.getFirst().getTime(), Matchers.equalTo(new Long[]{946684800000L, 946684920000L}));
        assertThat(queryResult.getFirst().getValue(), Matchers.equalTo(new Double[]{30.0, 1.0}));
        assertThat(queryResult.get(1).getLabel(), Matchers.equalTo("http://test/path2"));
        assertThat(queryResult.get(1).getTime(), Matchers.equalTo(new Long[]{946684800000L}));
        assertThat(queryResult.get(1).getValue(), Matchers.equalTo(new Double[]{30.0}));

        // Group by http status code
        queryResult = recordingTimeSeriesRepositoryRedis.query(
                new RecordingStatsService.StatsQuery()
                        .setOutputType(RecordingStatsService.StatsQuery.OutputType.COUNT)
                        .setGroupBy("status")
        ).block();

        assertThat(queryResult, Matchers.hasSize(2));
        assertThat(queryResult.getFirst().getLabel(), Matchers.equalTo("200"));
        assertThat(queryResult.getFirst().getTime(), Matchers.equalTo(new Long[]{946684800000L, 946684920000L}));
        assertThat(queryResult.getFirst().getValue(), Matchers.equalTo(new Double[]{30.0, 1.0}));
        assertThat(queryResult.get(1).getLabel(), Matchers.equalTo("503"));
        assertThat(queryResult.get(1).getTime(), Matchers.equalTo(new Long[]{946684800000L}));
        assertThat(queryResult.get(1).getValue(), Matchers.equalTo(new Double[]{30.0}));
    }

    @Test
    public void queryAvg() {
        /*
         * In the first minute there are 3 GET requests with average of 2
         */
        saveResponse("requestId-1", "routeId-1", "http://test/path1", "GET", "2000-01-01 00:00:03", 200, 1L);
        saveResponse("requestId-2", "routeId-1", "http://test/path1", "GET", "2000-01-01 00:00:20", 200, 2L);
        saveResponse("requestId-3", "routeId-1", "http://test/path1", "GET", "2000-01-01 00:00:47", 503, 3L);

        /*
         * In the third minute, there are 2 POST request with average of 4.5
         */
        saveResponse("requestId-4", "routeId-2", "http://test/path2", "POST", "2000-01-01 00:02:00", 200, 4L);
        saveResponse("requestId-5", "routeId-2", "http://test/path2", "POST", "2000-01-01 00:02:59", 503, 5L);

        // Group by routeId
        List<RecordingStatsService.StatsTimeSeries> queryResult = recordingTimeSeriesRepositoryRedis.query(
                new RecordingStatsService.StatsQuery()
                        .setOutputType(RecordingStatsService.StatsQuery.OutputType.AVG_RESPONSE_TIME)
                        .setGroupBy("routeId")
        ).block();

        assertThat(queryResult, Matchers.hasSize(2));
        assertThat(queryResult.getFirst().getLabel(), Matchers.equalTo("routeId-1"));
        assertThat(queryResult.getFirst().getTime(), Matchers.equalTo(new Long[]{946684800000L}));
        assertThat(queryResult.getFirst().getValue(), Matchers.equalTo(new Double[]{2.0}));
        assertThat(queryResult.get(1).getLabel(), Matchers.equalTo("routeId-2"));
        assertThat(queryResult.get(1).getTime(), Matchers.equalTo(new Long[]{946684920000L}));
        assertThat(queryResult.get(1).getValue(), Matchers.equalTo(new Double[]{4.5}));

        // Group by url
        queryResult = recordingTimeSeriesRepositoryRedis.query(
                new RecordingStatsService.StatsQuery()
                        .setOutputType(RecordingStatsService.StatsQuery.OutputType.AVG_RESPONSE_TIME)
                        .setGroupBy("url")
        ).block();
        assertThat(queryResult, Matchers.hasSize(2));
        assertThat(queryResult.getFirst().getLabel(), Matchers.equalTo("http://test/path1"));
        assertThat(queryResult.getFirst().getTime(), Matchers.equalTo(new Long[]{946684800000L}));
        assertThat(queryResult.getFirst().getValue(), Matchers.equalTo(new Double[]{2.0}));
        assertThat(queryResult.get(1).getLabel(), Matchers.equalTo("http://test/path2"));
        assertThat(queryResult.get(1).getTime(), Matchers.equalTo(new Long[]{946684920000L}));
        assertThat(queryResult.get(1).getValue(), Matchers.equalTo(new Double[]{4.5}));

        // Group by http method
        queryResult = recordingTimeSeriesRepositoryRedis.query(
                new RecordingStatsService.StatsQuery()
                        .setOutputType(RecordingStatsService.StatsQuery.OutputType.AVG_RESPONSE_TIME)
                        .setGroupBy("method")
        ).block();
        assertThat(queryResult, Matchers.hasSize(2));
        assertThat(queryResult.getFirst().getLabel(), Matchers.equalTo("GET"));
        assertThat(queryResult.getFirst().getTime(), Matchers.equalTo(new Long[]{946684800000L}));
        assertThat(queryResult.getFirst().getValue(), Matchers.equalTo(new Double[]{2.0}));
        assertThat(queryResult.get(1).getLabel(), Matchers.equalTo("POST"));
        assertThat(queryResult.get(1).getTime(), Matchers.equalTo(new Long[]{946684920000L}));
        assertThat(queryResult.get(1).getValue(), Matchers.equalTo(new Double[]{4.5}));

        // Group by status
        queryResult = recordingTimeSeriesRepositoryRedis.query(
                new RecordingStatsService.StatsQuery()
                        .setOutputType(RecordingStatsService.StatsQuery.OutputType.AVG_RESPONSE_TIME)
                        .setGroupBy("status")
        ).block();
        assertThat(queryResult, Matchers.hasSize(2));
        assertThat(queryResult.getFirst().getLabel(), Matchers.equalTo("200"));
        assertThat(queryResult.getFirst().getTime(), Matchers.equalTo(new Long[]{946684800000L, 946684920000L}));
        assertThat(queryResult.getFirst().getValue(), Matchers.equalTo(new Double[]{1.5, 4.0}));
        assertThat(queryResult.get(1).getLabel(), Matchers.equalTo("503"));
        assertThat(queryResult.get(1).getTime(), Matchers.equalTo(new Long[]{946684800000L, 946684920000L}));
        assertThat(queryResult.get(1).getValue(), Matchers.equalTo(new Double[]{3.0, 5.0}));
    }
}
