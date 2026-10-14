package hamedmoeeni.api_time_travel.infrastructure.persistence.recording;

import hamedmoeeni.api_time_travel.domain.recording.Recording;
import hamedmoeeni.api_time_travel.usecase.recording.RecordingSearch;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Iterator;

import static org.hamcrest.MatcherAssert.assertThat;

@Profile("infrastructure-test")
@SpringBootTest
public class RecordingRepositoryRedisITest {

    @Autowired
    private RecordingRepositoryRedisImpl recordingRepositoryRedis;
    @Autowired
    private ReactiveRedisConnectionFactory redisConnectionFactory;

    @BeforeEach
    public void setUp() {
        redisConnectionFactory.getReactiveConnection().serverCommands().flushAll().subscribe();
        recordingRepositoryRedis.init();
    }

    @Test
    public void saveRecordingTest() {
        Recording recording1 = new Recording()
                .setRequestId("testRequestId1")
                .setRouteId("testRouteId1")
                .setUrl("http://myHost/myPath1")
                .setMethod("testMethod1")
                .setDuration(1L)
                .setResponseStatus("testStatus1")
                .setTimestamp(ZonedDateTime.ofInstant(Instant.ofEpochMilli(ZonedDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli()),ZoneOffset.UTC));

        Recording recording2 = new Recording()
                .setRequestId("testRequestId2")
                .setRouteId("testRouteId1")
                .setUrl("http://myHost/myPath2")
                .setMethod("testMethod2")
                .setTimestamp(ZonedDateTime.ofInstant(Instant.ofEpochMilli(ZonedDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli()),ZoneOffset.UTC));

        Boolean upsertResult = recordingRepositoryRedis.upsertIgnoreResponse(
                new Recording()
                        .setRequestId(recording1.getRequestId())
                        .setRouteId(recording1.getRouteId())
                        .setUrl(recording1.getUrl())
                        .setMethod(recording1.getMethod())
                        .setTimestamp(recording1.getTimestamp())
        ).block();
        assertThat(upsertResult, Matchers.equalTo(true));

        Boolean upsertResponseResult = recordingRepositoryRedis.upsertIgnoreRequest(
                new Recording()
                        .setRequestId(recording1.getRequestId())
                        .setDuration(recording1.getDuration())
                        .setResponseStatus(recording1.getResponseStatus())
        ).block();
        assertThat(upsertResponseResult, Matchers.equalTo(true));

        recordingRepositoryRedis.upsertIgnoreResponse(
                new Recording()
                        .setRequestId(recording2.getRequestId())
                        .setRouteId(recording2.getRouteId())
                        .setUrl(recording2.getUrl())
                        .setMethod(recording2.getMethod())
                        .setTimestamp(recording2.getTimestamp())
        ).block();

        //All entities with sort
        recordingRepositoryRedis.search(
                        new RecordingSearch.RecordingSearchCriteria()
                                .setRouteId(recording1.getRouteId())
                                .setSortBy("requestId").setSortOrder("asc")
                )
                .collectList()
                .doOnSuccess(result -> {
                    assertThat(result, Matchers.iterableWithSize(2));

                    assertThat(result, Matchers.notNullValue());
                    Iterator<Recording> iterator = result.iterator();
                    Recording firstResult = iterator.next();
                    assertThat(firstResult, Matchers.equalTo(recording1));

                    Recording secondResult = iterator.next();
                    assertThat(secondResult, Matchers.equalTo(recording2));
                })
                .block();

        //Page size
        recordingRepositoryRedis.search(
                        new RecordingSearch.RecordingSearchCriteria()
                                .setRouteId(recording1.getRouteId())
                                .setSortBy("requestId").setSortOrder("asc")
                                .setPageSize(1)
                )
                .collectList()
                .doOnSuccess(result -> assertThat(result, Matchers.iterableWithSize(1)))
                .block();


        //Some entities
        recordingRepositoryRedis.search(
                        new RecordingSearch.RecordingSearchCriteria()
                                .setRequestId(recording1.getRequestId())
                )
                .collectList()
                .doOnSuccess(result -> {
                    assertThat(result, Matchers.iterableWithSize(1));

                    assertThat(result, Matchers.notNullValue());
                    Iterator<Recording> iterator = result.iterator();
                    Recording firstResult = iterator.next();
                    assertThat(firstResult, Matchers.equalTo(recording1));
                })
                .block();

        //No criteria
        recordingRepositoryRedis.search(
                        new RecordingSearch.RecordingSearchCriteria()
                )
                .collectList()
                .doOnSuccess(result -> assertThat(result, Matchers.iterableWithSize(2)))
                .block();

        //Timestamp range
        recordingRepositoryRedis.search(
                        new RecordingSearch.RecordingSearchCriteria()
                                .setFromTimestamp(recording1.getTimestamp().toInstant().toEpochMilli())
                                .setToTimestamp(recording2.getTimestamp().toInstant().toEpochMilli())
                )
                .collectList()
                .doOnSuccess(result -> assertThat(result, Matchers.iterableWithSize(2)))
                .block();
    }
}
