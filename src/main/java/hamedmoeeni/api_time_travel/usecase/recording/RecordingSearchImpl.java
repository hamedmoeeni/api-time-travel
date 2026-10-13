package hamedmoeeni.api_time_travel.usecase.recording;

import hamedmoeeni.api_time_travel.domain.recording.Recording;
import hamedmoeeni.api_time_travel.usecase.recording.port.RecordingPersistence;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
class RecordingSearchImpl implements RecordingSearch {
    private final RecordingPersistence recordingPersistence;

    RecordingSearchImpl(RecordingPersistence recordingPersistence) {
        this.recordingPersistence = recordingPersistence;
    }

    @Override
    public Flux<Recording> search(RecordingSearchCriteria criteria) {
        return recordingPersistence.search(criteria);
    }
}
