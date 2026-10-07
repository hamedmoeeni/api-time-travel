package hamedmoeeni.api_time_travel.adapter.rest.stats;

import hamedmoeeni.api_time_travel.usecase.stats.StatsManger;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("${att.management.uri-path}/stats")
public class StatsManagementController {
    private final StatsManger statsManger;

    public StatsManagementController(StatsManger statsManger) {
        this.statsManger = statsManger;
    }

    @PostMapping("/enable")
    public Mono<Void> enableStats() {
        return statsManger.enable(true).then(Mono.empty());
    }

    @PostMapping("/disable")
    public Mono<Void> disableStats() {
        return statsManger.enable(false).then(Mono.empty());
    }

}
