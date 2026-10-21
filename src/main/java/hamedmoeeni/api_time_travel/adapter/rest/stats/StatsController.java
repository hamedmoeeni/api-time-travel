package hamedmoeeni.api_time_travel.adapter.rest.stats;

import hamedmoeeni.api_time_travel.usecase.recording.RecordingStatsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("${att.management.uri-path}/stats")
class StatsController {
    private final RecordingStatsService recordingStatsService;
    private final QueryLabelsDtoMapper queryLabelsDtoMapper;
    private final StatsQueryDtoMapper statsQueryDtoMapper;
    private final StatsTimeSeriesDtoMapper statsTimeSeriesDtoMapper;

    StatsController(RecordingStatsService recordingStatsService, QueryLabelsDtoMapper queryLabelsDtoMapper, StatsQueryDtoMapper statsQueryDtoMapper, StatsTimeSeriesDtoMapper statsTimeSeriesDtoMapper) {
        this.recordingStatsService = recordingStatsService;
        this.queryLabelsDtoMapper = queryLabelsDtoMapper;
        this.statsQueryDtoMapper = statsQueryDtoMapper;
        this.statsTimeSeriesDtoMapper = statsTimeSeriesDtoMapper;
    }

    @GetMapping("/labels")
    public Mono<QueryLabelsDto> getLabels() {
        return recordingStatsService.getQueryLabels().map(queryLabelsDtoMapper::toDto);
    }

    @GetMapping("/timeseries")
    public Mono<List<StatsTimeSeriesDto>> getStats(StatsQueryDto statsQueryDto) {
        return recordingStatsService.queryTimeSeries(statsQueryDtoMapper.fromDto(statsQueryDto))
                .map(statsTimeSeries -> statsTimeSeries.stream().map(statsTimeSeriesDtoMapper::toDto).collect(Collectors.toList()));
    }
}
