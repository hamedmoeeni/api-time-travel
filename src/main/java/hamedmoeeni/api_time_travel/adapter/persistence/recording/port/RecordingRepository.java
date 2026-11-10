package hamedmoeeni.api_time_travel.adapter.persistence.recording.port;

import hamedmoeeni.api_time_travel.domain.recording.Recording;
import hamedmoeeni.api_time_travel.usecase.recording.RecordingSearchService;
import org.springframework.data.domain.Page;
import reactor.core.publisher.Mono;

public interface RecordingRepository {
    Mono<Boolean> upsertIgnoreResponse(Recording recording);

    Mono<Boolean> upsertIgnoreRequest(Recording recording);

    Mono<Page<Recording>> search(RecordingSearchService.RecordingSearchCriteria criteria);
}
