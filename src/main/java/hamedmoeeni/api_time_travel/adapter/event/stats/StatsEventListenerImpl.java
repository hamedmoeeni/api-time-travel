package hamedmoeeni.api_time_travel.adapter.event.stats;

import hamedmoeeni.api_time_travel.domain.stats.Stats;
import hamedmoeeni.api_time_travel.usecase.stats.StatsManger;
import org.springframework.stereotype.Service;

@Service
class StatsEventListenerImpl implements StatsEventListener{
    private final StatsManger statsManger;

    StatsEventListenerImpl(StatsManger statsManger) {
        this.statsManger = statsManger;
    }

    @Override
    public void onStats(Stats stats) {
        statsManger.saveStats(stats);
    }
}
