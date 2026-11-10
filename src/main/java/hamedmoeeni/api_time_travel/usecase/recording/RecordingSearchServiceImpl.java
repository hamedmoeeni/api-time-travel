package hamedmoeeni.api_time_travel.usecase.recording;

import hamedmoeeni.api_time_travel.domain.recording.Recording;
import hamedmoeeni.api_time_travel.usecase.Page;
import hamedmoeeni.api_time_travel.usecase.PageMapper;
import hamedmoeeni.api_time_travel.usecase.recording.port.RecordingPersistence;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
class RecordingSearchServiceImpl implements RecordingSearchService {
    private final RecordingPersistence recordingPersistence;
    private final PageMapper pageMapper;

    RecordingSearchServiceImpl(RecordingPersistence recordingPersistence, PageMapper pageMapper) {
        this.recordingPersistence = recordingPersistence;
        this.pageMapper = pageMapper;
    }

    @Override
    public Mono<Page<Recording>> search(RecordingSearchCriteria criteria) {
        return recordingPersistence.search(criteria).map(pageMapper::mapFromSpringDataPage);
    }
}
