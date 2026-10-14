package hamedmoeeni.api_time_travel.adapter.rest.recording;

import hamedmoeeni.api_time_travel.usecase.recording.RecordingSearch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
interface CriteriaDtoMapper {

    @Mapping(source = "page",target = "pageStart")
    @Mapping(source = "limit",target = "pageSize")
    RecordingSearch.RecordingSearchCriteria fromCriteriaDto(CriteriaDto criteriaDto);

}
