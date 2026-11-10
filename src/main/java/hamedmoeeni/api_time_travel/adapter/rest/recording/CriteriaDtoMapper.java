package hamedmoeeni.api_time_travel.adapter.rest.recording;

import hamedmoeeni.api_time_travel.usecase.recording.RecordingSearchService;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface CriteriaDtoMapper {


    RecordingSearchService.RecordingSearchCriteria fromCriteriaDto(CriteriaDto criteriaDto);

}
