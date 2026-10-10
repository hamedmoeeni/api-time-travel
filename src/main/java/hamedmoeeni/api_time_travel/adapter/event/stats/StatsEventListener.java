package hamedmoeeni.api_time_travel.adapter.event.stats;

import hamedmoeeni.api_time_travel.domain.stats.Stats;

public interface StatsEventListener {
    void onStats(Stats stats);

}
