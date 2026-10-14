package hamedmoeeni.api_time_travel.infrastructure.persistence.recording;

import hamedmoeeni.api_time_travel.domain.recording.Recording;
import hamedmoeeni.api_time_travel.usecase.recording.RecordingSearch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;

@Mapper(componentModel = "spring")
interface RecordingEntityMapper {


    @Named("zonedDataTimeToEpochMilli")
    static Long zonedDataTimeToUtcString(ZonedDateTime zonedDateTime) {
        return zonedDateTime == null ? null : zonedDateTime.toInstant().toEpochMilli();
    }

    @Named("timestampToZonedDateTime")
    static ZonedDateTime timestampToZonedDateTime(Long timestamp) {
        return timestamp == null ? null : ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC);
    }

    /*@Mapping(source = "timestamp", target = "utcTimestamp", qualifiedByName = "zonedDataTimeToUtcString")
    RecordingEntity toRecordingEntity(Recording recording);*/

    @Mapping(source = "timestamp", target = "timestamp", qualifiedByName = "timestampToZonedDateTime")
    Recording fromRecordingEntity(RecordingEntity recordingEntity);

    @Mapping(source = "timestamp", target = "timestamp", qualifiedByName = "zonedDataTimeToEpochMilli")
    RecordingEntityRequestDetails toRequestDetails(Recording recording);

    RecordingEntityResponseDetails toResponseDetails(Recording recording);

    default String criteriaToRedisQuery(RecordingSearch.RecordingSearchCriteria criteria) {
        StringBuilder builder = new StringBuilder();
        if (criteria != null) {
            if (criteria.getRequestId() != null && !criteria.getRequestId().isBlank()) {
                builder.append(" ").append("@requestId:'").append(criteria.getRequestId()).append("'");
            }
            if (criteria.getRouteId() != null && !criteria.getRouteId().isBlank()) {
                builder.append(" ").append("@routeId:'").append(criteria.getRouteId()).append("'");
            }
            if (criteria.getResponseStatus() != null && !criteria.getResponseStatus().isBlank()) {
                builder.append(" ").append("@responseStatus:'").append(criteria.getResponseStatus()).append("'");
            }
            if (criteria.getFromTimestamp() != null || criteria.getToTimestamp() != null) {
                builder.append(" ").append("@timestamp:[")
                        .append(criteria.getFromTimestamp() == null ? "-inf" : criteria.getFromTimestamp())
                        .append(" ")
                        .append(criteria.getToTimestamp() == null ? "+inf" : criteria.getToTimestamp())
                        .append("]");
            }
        }
        return builder.isEmpty() ? "*" : builder.toString().trim();
    }

    default Map.Entry<String, String> criteriaToRedisSort(RecordingSearch.RecordingSearchCriteria criteria) {
        String sort = "timestamp";
        String direction = "DESC";
        if (criteria != null && criteria.getSortBy() != null) {
            if (criteria.getSortBy().equalsIgnoreCase("requestId")) {
                sort = "requestId";
            } else if (criteria.getSortBy().equalsIgnoreCase("duration")) {
                sort = "duration";
            }
        }

        if (criteria != null && criteria.getSortOrder() != null && criteria.getSortOrder().equalsIgnoreCase("asc")) {
            direction = "ASC";
        }

        return Map.entry(sort, direction);
    }
}
