package hamedmoeeni.api_time_travel.adapter.rest.recording;

import hamedmoeeni.api_time_travel.usecase.recording.RecordingManager;
import hamedmoeeni.api_time_travel.usecase.recording.RecordingSearchService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("${att.management.uri-path}/recording")
class RecordingManagementController {
    private final RecordingManager recordingManager;
    private final RecordingSearchService recordingSearchService;
    private final CriteriaDtoMapper criteriaDtoMapper;
    private final RecordingDtoMapper recordingDtoMapper;

    public RecordingManagementController(RecordingManager recordingManager, RecordingSearchService recordingSearchService, CriteriaDtoMapper criteriaDtoMapper, RecordingDtoMapper recordingDtoMapper) {
        this.recordingManager = recordingManager;
        this.recordingSearchService = recordingSearchService;
        this.criteriaDtoMapper = criteriaDtoMapper;
        this.recordingDtoMapper = recordingDtoMapper;
    }

    @PostMapping("/enable")
    public Mono<Void> enableStats() {
        return recordingManager.enableRecording(true).then(Mono.empty());
    }

    @PostMapping("/disable")
    public Mono<Void> disableStats() {
        return recordingManager.enableRecording(false).then(Mono.empty());
    }

    @GetMapping
    public Flux<RecordingDto> search(CriteriaDto criteriaDto) {
        return recordingSearchService.search(criteriaDtoMapper.fromCriteriaDto(criteriaDto))
                .map(recordingDtoMapper::toRecordingDto);
    }
}
