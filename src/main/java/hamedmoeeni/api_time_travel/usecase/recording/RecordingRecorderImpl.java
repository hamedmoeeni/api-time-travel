package hamedmoeeni.api_time_travel.usecase.recording;

import hamedmoeeni.api_time_travel.domain.recording.Recording;
import hamedmoeeni.api_time_travel.usecase.recording.port.RecordingPersistence;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
class RecordingRecorderImpl implements RecordingRecorder {
    private final RecordingPersistence recordingPersistence;

    RecordingRecorderImpl(RecordingPersistence recordingPersistence) {
        this.recordingPersistence = recordingPersistence;
    }

    @Override
    public Mono<Boolean> saveRecording(Recording recording) {
        return recordingPersistence.saveRecording(recording);
    }
}
