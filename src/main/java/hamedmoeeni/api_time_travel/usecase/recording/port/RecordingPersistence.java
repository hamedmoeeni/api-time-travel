package hamedmoeeni.api_time_travel.usecase.recording.port;

import hamedmoeeni.api_time_travel.domain.recording.Recording;
import hamedmoeeni.api_time_travel.usecase.recording.RecordingSearchService;
import hamedmoeeni.api_time_travel.usecase.recording.RecordingStatsService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface RecordingPersistence {

    Mono<Boolean> saveRecording(Recording recording);

    Flux<Recording> search(RecordingSearchService.RecordingSearchCriteria criteria);

    Mono<RecordingStatsService.QueryLabels> getTimeSeriesQueryLabels();

    Mono<List<RecordingStatsService.StatsTimeSeries>> queryTimeSeries(RecordingStatsService.StatsQuery statsQuery);
}
