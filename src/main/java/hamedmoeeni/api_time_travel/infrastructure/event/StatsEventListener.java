package hamedmoeeni.api_time_travel.infrastructure.event;

import hamedmoeeni.api_time_travel.domain.stats.Stats;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class StatsEventListener {
    private final hamedmoeeni.api_time_travel.adapter.event.stats.StatsEventListener statsEventListener;

    public StatsEventListener(hamedmoeeni.api_time_travel.adapter.event.stats.StatsEventListener statsEventListener) {
        this.statsEventListener = statsEventListener;
    }

    @EventListener(Stats.class)
    public void onStatsEvent(Stats stats){
        this.statsEventListener.onStats(stats);
    }
}
