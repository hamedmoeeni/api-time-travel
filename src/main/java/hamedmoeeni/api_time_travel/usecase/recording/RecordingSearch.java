package hamedmoeeni.api_time_travel.usecase.recording;

import hamedmoeeni.api_time_travel.domain.recording.Recording;
import lombok.Data;
import reactor.core.publisher.Flux;

public interface RecordingSearch {
    Flux<Recording> search(RecordingSearchCriteria criteria);

    @Data
    class RecordingSearchCriteria {
        private String requestId;
        private String routeId;
        private String responseStatus;
        private Long fromTimestamp;
        private Long toTimestamp;
        private String sortBy;
        private String sortOrder;
        private long pageStart = 0;
        private long pageSize = 20;
    }
}
