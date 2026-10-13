package hamedmoeeni.api_time_travel.adapter.event.recording;

import hamedmoeeni.api_time_travel.domain.recording.Recording;
import hamedmoeeni.api_time_travel.usecase.recording.RecordingRecorder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
class RecordingEventListenerImpl implements RecordingEventListener {
    private final RecordingRecorder recordingRecorder;

    RecordingEventListenerImpl(RecordingRecorder recordingRecorder) {
        this.recordingRecorder = recordingRecorder;
    }

    @Override
    public Mono<Boolean> onStats(Recording recording) {
        return recordingRecorder.saveRecording(recording);
    }
}
