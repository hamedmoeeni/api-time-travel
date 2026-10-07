package hamedmoeeni.api_time_travel.adapter.rest.playback;

import hamedmoeeni.api_time_travel.usecase.stats.StatsManger;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("${att.management.uri-path}/playback")
public class PlaybackManagementController {
    private final StatsManger statsManger;

    public PlaybackManagementController(StatsManger statsManger) {
        this.statsManger = statsManger;
    }

    @PostMapping("/enable")
    public Mono<Void> enableStats() {
        return statsManger.enablePlayback(true).then(Mono.empty());
    }

    @PostMapping("/disable")
    public Mono<Void> disableStats() {
        return statsManger.enablePlayback(false).then(Mono.empty());
    }
}
