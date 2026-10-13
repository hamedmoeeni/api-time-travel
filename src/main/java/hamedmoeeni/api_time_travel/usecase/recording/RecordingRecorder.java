package hamedmoeeni.api_time_travel.usecase.recording;

import hamedmoeeni.api_time_travel.domain.recording.Recording;
import reactor.core.publisher.Mono;

public interface RecordingRecorder {
    Mono<Boolean> saveRecording(Recording recording);
}
