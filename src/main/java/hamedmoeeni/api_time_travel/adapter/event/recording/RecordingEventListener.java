package hamedmoeeni.api_time_travel.adapter.event.recording;

import hamedmoeeni.api_time_travel.domain.recording.Recording;
import reactor.core.publisher.Mono;

public interface RecordingEventListener {
    Mono<Boolean> onStats(Recording recording);

}
