package hamedmoeeni.api_time_travel.adapter.rest.stats;

import hamedmoeeni.api_time_travel.usecase.recording.RecordingStatsService;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface QueryLabelsDtoMapper {
    QueryLabelsDto toDto(RecordingStatsService.QueryLabels queryLabels);
}
