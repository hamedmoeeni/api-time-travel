package hamedmoeeni.api_time_travel.adapter.persistence.recording;

import hamedmoeeni.api_time_travel.adapter.persistence.recording.port.RecordingRepository;
import hamedmoeeni.api_time_travel.domain.recording.Recording;
import hamedmoeeni.api_time_travel.usecase.recording.RecordingSearch;
import hamedmoeeni.api_time_travel.usecase.recording.port.RecordingPersistence;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
class RecordingPersistenceImpl implements RecordingPersistence {
    private final RecordingRepository recordingRepository;

    public RecordingPersistenceImpl(RecordingRepository recordingRepository) {
        this.recordingRepository = recordingRepository;
    }

    @Override
    public Mono<Boolean> saveRecording(Recording recording) {
        //Upsert request
        if(recording.getResponseStatus()==null) {
            return this.recordingRepository.upsertIgnoreResponse(recording);
        }
        //Upsert response
        return this.recordingRepository.upsertIgnoreRequest(recording);
    }

    @Override
    public Flux<Recording> search(RecordingSearch.RecordingSearchCriteria criteria) {
        return this.recordingRepository.search(criteria);
    }
}
