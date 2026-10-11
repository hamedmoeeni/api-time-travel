package hamedmoeeni.api_time_travel.adapter.persistence.stats;

import hamedmoeeni.api_time_travel.adapter.persistence.stats.port.StatsRepository;
import hamedmoeeni.api_time_travel.domain.stats.Stats;
import hamedmoeeni.api_time_travel.usecase.stats.port.StatsPersistence;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class StatsPersistenceImpl implements StatsPersistence {
    private final StatsRepository statsRepository;

    public StatsPersistenceImpl(StatsRepository statsRepository) {
        this.statsRepository = statsRepository;
    }

    @Override
    public Mono<Boolean> saveStats(Stats stats) {
        //Upsert request
        if(stats.getResponseStatus()==null) {
            return this.statsRepository.upsertIgnoreResponse(stats);
        }
        //Upsert response
        return this.statsRepository.upsertIgnoreRequest(stats);
    }
}
