package hamedmoeeni.api_time_travel.adapter.collector.recording.port;

import reactor.core.publisher.Mono;

public interface RecordingDataCollectorInfrastructure {
    Mono<Boolean> enable(boolean enable);

    Mono<Boolean> enablePlayback(boolean enable);
}
