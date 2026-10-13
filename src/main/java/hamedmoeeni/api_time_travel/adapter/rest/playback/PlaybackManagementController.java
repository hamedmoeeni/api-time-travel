package hamedmoeeni.api_time_travel.adapter.rest.playback;

import hamedmoeeni.api_time_travel.usecase.recording.RecordingManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("${att.management.uri-path}/playback")
public class PlaybackManagementController {
    private final RecordingManager recordingManager;

    public PlaybackManagementController(RecordingManager recordingManager) {
        this.recordingManager = recordingManager;
    }

    @PostMapping("/enable")
    public Mono<Void> enableStats() {
        return recordingManager.enablePlayback(true).then(Mono.empty());
    }

    @PostMapping("/disable")
    public Mono<Void> disableStats() {
        return recordingManager.enablePlayback(false).then(Mono.empty());
    }
}
