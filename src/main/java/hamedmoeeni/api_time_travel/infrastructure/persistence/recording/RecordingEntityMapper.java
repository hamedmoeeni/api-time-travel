package hamedmoeeni.api_time_travel.infrastructure.persistence.recording;

import hamedmoeeni.api_time_travel.domain.recording.Recording;
import hamedmoeeni.api_time_travel.usecase.recording.RecordingSearch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Mapper(componentModel = "spring")
interface RecordingEntityMapper {


    @Named("zonedDataTimeToUtcString")
    static String zonedDataTimeToUtcString(ZonedDateTime zonedDateTime) {
        if (zonedDateTime == null) {
            return null;
        }
        return zonedDateTime.withZoneSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME);
    }

    /*@Mapping(source = "timestamp", target = "utcTimestamp", qualifiedByName = "zonedDataTimeToUtcString")
    RecordingEntity toRecordingEntity(Recording recording);*/

    Recording fromRecordingEntity(RecordingEntity recordingEntity);

    @Mapping(source = "timestamp", target = "utcTimestamp", qualifiedByName = "zonedDataTimeToUtcString")
    RecordingEntityRequestDetails toRequestDetails(Recording recording);

    RecordingEntityResponseDetails toResponseDetails(Recording recording);

    default String criteriaToRedisQuery(RecordingSearch.RecordingSearchCriteria criteria) {
        StringBuilder builder = new StringBuilder();
        if (criteria != null) {
            if (criteria.getRequestId() != null && !criteria.getRequestId().isBlank()) {
                builder.append(" @requestId:'").append(criteria.getRequestId()).append("'");
            }
            if (criteria.getRouteId() != null && !criteria.getRouteId().isBlank()) {
                builder.append(" @routeId:'").append(criteria.getRouteId()).append("'");
            }
            if (criteria.getResponseStatus() != null && !criteria.getResponseStatus().isBlank()) {
                builder.append(" @responseStatus:'").append(criteria.getResponseStatus()).append("'");
            }
        }
        return builder.isEmpty() ? "*" : builder.toString().trim();
    }

    default Map.Entry<String, String> criteriaToRedisSort(RecordingSearch.RecordingSearchCriteria criteria) {
        if (criteria != null) {
            if (criteria.getOrderByRequestId() != null) {
                return Map.entry("requestId", criteria.getOrderByRequestId() > 0 ? "ASC" : "DESC");
            }
            if (criteria.getOrderByRouteId() != null) {
                return Map.entry("routeId", criteria.getOrderByRouteId() > 0 ? "ASC" : "DESC");
            }
            if (criteria.getOrderByResponseStatus() != null) {
                return Map.entry("responseStatus", criteria.getOrderByResponseStatus() > 0 ? "ASC" : "DESC");
            }
        }
        return Map.entry("requestId", "DESC");
    }
}
