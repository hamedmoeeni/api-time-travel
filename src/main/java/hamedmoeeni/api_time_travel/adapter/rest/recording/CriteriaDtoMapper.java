package hamedmoeeni.api_time_travel.adapter.rest.recording;

import hamedmoeeni.api_time_travel.usecase.recording.RecordingSearchService;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
interface CriteriaDtoMapper {

    @Mapping(source = "page",target = "pageStart")
    @Mapping(source = "limit",target = "pageSize")
    RecordingSearchService.RecordingSearchCriteria fromCriteriaDto(CriteriaDto criteriaDto);

}
