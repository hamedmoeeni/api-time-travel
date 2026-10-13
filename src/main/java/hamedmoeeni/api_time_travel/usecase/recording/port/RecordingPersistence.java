package hamedmoeeni.api_time_travel.usecase.recording.port;

import hamedmoeeni.api_time_travel.domain.recording.Recording;
import hamedmoeeni.api_time_travel.usecase.recording.RecordingSearch;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface RecordingPersistence {

    Mono<Boolean> saveRecording(Recording recording);

    Flux<Recording> search(RecordingSearch.RecordingSearchCriteria criteria);
}
