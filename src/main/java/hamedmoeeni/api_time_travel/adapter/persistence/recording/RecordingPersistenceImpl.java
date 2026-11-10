package hamedmoeeni.api_time_travel.adapter.persistence.recording;

import hamedmoeeni.api_time_travel.adapter.persistence.recording.port.RecordingRepository;
import hamedmoeeni.api_time_travel.adapter.persistence.recording.port.RecordingTimeSeriesRepository;
import hamedmoeeni.api_time_travel.domain.recording.Recording;
import hamedmoeeni.api_time_travel.usecase.recording.RecordingSearchService;
import hamedmoeeni.api_time_travel.usecase.recording.RecordingStatsService;
import hamedmoeeni.api_time_travel.usecase.recording.port.RecordingPersistence;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
class RecordingPersistenceImpl implements RecordingPersistence {
    private final RecordingRepository recordingRepository;
    private final RecordingTimeSeriesRepository recordingTimeSeriesRepository;

    public RecordingPersistenceImpl(RecordingRepository recordingRepository, RecordingTimeSeriesRepository recordingTimeSeriesRepository) {
        this.recordingRepository = recordingRepository;
        this.recordingTimeSeriesRepository = recordingTimeSeriesRepository;
    }

    @Override
    public Mono<Boolean> saveRecording(Recording recording) {
        //Upsert request
        if (recording.getResponseStatus() == null) {
            return this.recordingRepository.upsertIgnoreResponse(recording)
                    .then(recordingTimeSeriesRepository.saveRecordingIgnoreResponse(recording));
        }
        //Upsert response
        return this.recordingRepository.upsertIgnoreRequest(recording)
                .then(recordingTimeSeriesRepository.saveRecordingIgnoreRqquest(recording));
    }

    @Override
    public Mono<Page<Recording>> search(RecordingSearchService.RecordingSearchCriteria criteria) {
        return this.recordingRepository.search(criteria);
    }

    @Override
    public Mono<RecordingStatsService.QueryLabels> getTimeSeriesQueryLabels() {
        return recordingTimeSeriesRepository.getQueryLabels();
    }

    @Override
    public Mono<List<RecordingStatsService.StatsTimeSeries>> queryTimeSeries(RecordingStatsService.StatsQuery statsQuery) {
        return recordingTimeSeriesRepository.query(statsQuery);
    }
}
