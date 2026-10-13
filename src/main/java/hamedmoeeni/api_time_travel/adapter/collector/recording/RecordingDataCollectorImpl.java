package hamedmoeeni.api_time_travel.adapter.collector.recording;

import hamedmoeeni.api_time_travel.adapter.collector.recording.port.RecordingDataCollectorInfrastructure;
import hamedmoeeni.api_time_travel.usecase.recording.port.RecordingDataCollector;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
class RecordingDataCollectorImpl implements RecordingDataCollector {
    private final RecordingDataCollectorInfrastructure recordingDataCollectorInfrastructure;

    public RecordingDataCollectorImpl(RecordingDataCollectorInfrastructure recordingDataCollectorInfrastructure) {
        this.recordingDataCollectorInfrastructure = recordingDataCollectorInfrastructure;
    }

    @Override
    public Mono<Boolean> enableRecording(boolean enable) {
        return recordingDataCollectorInfrastructure.enable(enable);
    }

    @Override
    public Mono<Boolean> enablePlayback(boolean enable) {
        return recordingDataCollectorInfrastructure.enablePlayback(enable);
    }
}
