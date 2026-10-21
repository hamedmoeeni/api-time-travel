package hamedmoeeni.api_time_travel.usecase.recording;

import hamedmoeeni.api_time_travel.usecase.recording.port.RecordingPersistence;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class RecordingStatsServiceImpl implements RecordingStatsService {
    private final RecordingPersistence recordingPersistence;

    public RecordingStatsServiceImpl(RecordingPersistence recordingPersistence) {
        this.recordingPersistence = recordingPersistence;
    }

    @Override
    public Mono<QueryLabels> getQueryLabels() {
        return recordingPersistence.getTimeSeriesQueryLabels();
    }

    @Override
    public Mono<List<StatsTimeSeries>> queryTimeSeries(StatsQuery statsQuery) {
        return recordingPersistence.queryTimeSeries(statsQuery);
    }
}
