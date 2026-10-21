package hamedmoeeni.api_time_travel.adapter.persistence.recording.port;

import hamedmoeeni.api_time_travel.domain.recording.Recording;
import hamedmoeeni.api_time_travel.usecase.recording.RecordingStatsService;
import reactor.core.publisher.Mono;

import java.util.List;

public interface RecordingTimeSeriesRepository {
    Mono<Boolean> saveRecordingIgnoreResponse(Recording recording);
    Mono<Boolean> saveRecordingIgnoreRqquest(Recording recording);

    Mono<RecordingStatsService.QueryLabels> getQueryLabels();

    Mono<List<RecordingStatsService.StatsTimeSeries>> query(RecordingStatsService.StatsQuery statsQuery);
}
