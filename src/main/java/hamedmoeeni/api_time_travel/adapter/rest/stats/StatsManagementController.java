package hamedmoeeni.api_time_travel.adapter.rest.stats;

import hamedmoeeni.api_time_travel.usecase.recording.RecordingManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("${att.management.uri-path}/stats")
public class StatsManagementController {
    private final RecordingManager recordingManager;

    public StatsManagementController(RecordingManager recordingManager) {
        this.recordingManager = recordingManager;
    }

    @PostMapping("/enable")
    public Mono<Void> enableStats() {
        return recordingManager.enableRecording(true).then(Mono.empty());
    }

    @PostMapping("/disable")
    public Mono<Void> disableStats() {
        return recordingManager.enableRecording(false).then(Mono.empty());
    }

}
