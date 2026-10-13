package hamedmoeeni.api_time_travel.usecase.recording;

import reactor.core.publisher.Mono;

public interface RecordingManager {
    Mono<Boolean> enableRecording(boolean enable);

    Mono<Boolean> enablePlayback(boolean enable);
}
