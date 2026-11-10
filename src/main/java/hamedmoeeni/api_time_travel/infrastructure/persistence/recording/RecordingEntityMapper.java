package hamedmoeeni.api_time_travel.infrastructure.persistence.recording;

import hamedmoeeni.api_time_travel.domain.recording.Recording;
import hamedmoeeni.api_time_travel.usecase.recording.RecordingSearchService;
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

    private void createOrQuery(StringBuilder stringBuilder, String fieldName, String[] values) {
        if (values != null && values.length > 0) {
            stringBuilder.append(" ").append("@").append(fieldName).append(":{");
            for (String value : values) {
                stringBuilder.append("'")
                        .append(value
                                .replaceAll("-", "\\\\-")
                                .replaceAll("@", "\\\\@")
                        ).append("'").append("|");
            }
            if (stringBuilder.charAt(stringBuilder.length() - 1) == '|') {
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            }
            stringBuilder.append("}");
        }
    }

    private void createOrQuery(StringBuilder stringBuilder, @SuppressWarnings("SameParameterValue") String fieldName, Integer[] values) {
        if (values != null && values.length > 0) {
            stringBuilder.append(" ").append("@").append(fieldName).append(":[");
            if (values.length == 1) {
                stringBuilder.append(values[0]).append(" ").append(values[0]);
            } else if (values.length == 2) {
                stringBuilder.append(values[0]).append(" ").append(values[1]);
            }
            stringBuilder.append("]");
        }
    }

    @Mapping(source = "timestamp", target = "timestamp", qualifiedByName = "timestampToZonedDateTime")
    Recording fromRecordingEntity(RecordingEntity recordingEntity);

    @Mapping(source = "timestamp", target = "timestamp", qualifiedByName = "zonedDataTimeToEpochMilli")
    RecordingEntityRequestDetails toRequestDetails(Recording recording);

    RecordingEntityResponseDetails toResponseDetails(Recording recording);

    default String criteriaToRedisQuery(RecordingSearchService.RecordingSearchCriteria criteria) {
        StringBuilder builder = new StringBuilder();
        if (criteria != null) {
            createOrQuery(builder, "requestId", criteria.getRequestId());
            createOrQuery(builder, "routeId", criteria.getRouteId());
            createOrQuery(builder, "responseStatus", criteria.getResponseStatus());
            createOrQuery(builder, "method", criteria.getMethod());
            if (criteria.getFromTimestamp() != null || criteria.getToTimestamp() != null) {
                builder.append(" ").append("@timestamp:[")
                        .append(criteria.getFromTimestamp() == null ? "-inf" : criteria.getFromTimestamp())
                        .append(" ")
                        .append(criteria.getToTimestamp() == null ? "+inf" : criteria.getToTimestamp())
                        .append("]");
            }
        }
        return builder.isEmpty() ? "*" : builder.insert(0, "'").append("'").toString().trim();
    }

    default Map.Entry<String, String> criteriaToRedisSort(RecordingSearchService.RecordingSearchCriteria criteria) {
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
