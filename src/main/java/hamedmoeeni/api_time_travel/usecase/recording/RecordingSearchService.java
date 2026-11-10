package hamedmoeeni.api_time_travel.usecase.recording;

import hamedmoeeni.api_time_travel.domain.recording.Recording;
import hamedmoeeni.api_time_travel.usecase.Page;
import lombok.Data;
import reactor.core.publisher.Mono;

public interface RecordingSearchService {
    Mono<Page<Recording>> search(RecordingSearchCriteria criteria);

    @Data
    class RecordingSearchCriteria {
        private String[] requestId;
        private String[] routeId;
        private Integer[] responseStatus;
        private String[] method;
        private Long fromTimestamp;
        private Long toTimestamp;
        private String sortBy;
        private String sortOrder;
        private long pageNumber = 0;
        private long pageSize = 20;
    }
}
