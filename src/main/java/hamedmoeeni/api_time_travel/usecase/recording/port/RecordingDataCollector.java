package hamedmoeeni.api_time_travel.usecase.recording.port;

import reactor.core.publisher.Mono;

public interface RecordingDataCollector {
    Mono<Boolean> enableRecording(boolean enable);

    Mono<Boolean> enablePlayback(boolean enable);

}
