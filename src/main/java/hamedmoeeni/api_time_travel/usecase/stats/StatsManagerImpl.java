package hamedmoeeni.api_time_travel.usecase.stats;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class StatsManagerImpl implements StatsManger{
    private final StatsAdapter statsAdapter;

    public StatsManagerImpl(StatsAdapter statsAdapter) {
        this.statsAdapter = statsAdapter;
    }

    @Override
    public Mono<Boolean> enable(boolean enable) {
        return statsAdapter.enable(enable);
    }

    @Override
    public Mono<Boolean> enablePlayback(boolean enable) {
        return statsAdapter.enablePlayback(enable);
    }
}
