package hamedmoeeni.api_time_travel.adapter.persistence.recording.port;

import hamedmoeeni.api_time_travel.domain.recording.Recording;
import hamedmoeeni.api_time_travel.usecase.recording.RecordingSearch;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RecordingRepository {
    Mono<Boolean> upsertIgnoreResponse(Recording recording);

    Mono<Boolean> upsertIgnoreRequest(Recording recording);

    Flux<Recording> search(RecordingSearch.RecordingSearchCriteria criteria);
}
