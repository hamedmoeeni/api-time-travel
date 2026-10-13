package hamedmoeeni.api_time_travel.usecase.recording;

import hamedmoeeni.api_time_travel.usecase.recording.port.RecordingDataCollector;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
class RecordingManagerImpl implements RecordingManager {
    private final RecordingDataCollector recordingDataCollector;


    public RecordingManagerImpl(RecordingDataCollector recordingDataCollector) {
        this.recordingDataCollector = recordingDataCollector;
    }

    @Override
    public Mono<Boolean> enableRecording(boolean enable) {
        return recordingDataCollector.enableRecording(enable);
    }

    @Override
    public Mono<Boolean> enablePlayback(boolean enable) {
        return recordingDataCollector.enablePlayback(enable);
    }


}
