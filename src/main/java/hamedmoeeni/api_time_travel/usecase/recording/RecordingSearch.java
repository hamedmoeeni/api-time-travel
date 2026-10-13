package hamedmoeeni.api_time_travel.usecase.recording;

import hamedmoeeni.api_time_travel.domain.recording.Recording;
import lombok.Data;
import reactor.core.publisher.Flux;

import java.time.ZonedDateTime;

public interface RecordingSearch {
    Flux<Recording> search(RecordingSearchCriteria criteria);

    @Data
    class RecordingSearchCriteria {
        private String requestId;
        private String routeId;
        private String responseStatus;
        private ZonedDateTime fromTimestamp;
        private ZonedDateTime toTimestamp;
        private Integer orderByRequestId;
        private Integer orderByRouteId;
        private Integer orderByResponseStatus;
        private long pageStart = 0;
        private long pageSize = 20;
    }
}
