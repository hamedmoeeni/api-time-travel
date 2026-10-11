package hamedmoeeni.api_time_travel.infrastructure.persistence.stats;

import hamedmoeeni.api_time_travel.domain.stats.Stats;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Mapper(componentModel = "spring")
public interface StatsEntityMapper {

    @Mapping(source = "timestamp", target = "utcTimestamp", qualifiedByName = "zonedDataTimeToUtcString")
    StatsEntity toStatsEntity(Stats stats);

    @Named("zonedDataTimeToUtcString")
    static String zonedDataTimeToUtcString(ZonedDateTime zonedDateTime) {
        if(zonedDateTime==null){
            return null;
        }
        return zonedDateTime.withZoneSameInstant(ZoneOffset.UTC).format(DateTimeFormatter.ISO_DATE_TIME);
    }

    default void updateRequestDetails(StatsEntity target, StatsEntity source) {
        if (target == null) {
            return;
        }
        target
                .setRequestId(source.getRequestId())
                .setRouteId(source.getRouteId())
                .setUtcTimestamp(source.getUtcTimestamp())
                .setUrl(source.getUrl())
                .setMethod(source.getMethod())
                .setRequestHeaders(source.getRequestHeaders())
                .setRequestBody(source.getRequestBody());
    }

    default void updateResponseDetails(StatsEntity target, StatsEntity source) {
        if (target == null) {
            return;
        }
        target.setDuration(source.getDuration())
                .setResponseStatus(source.getResponseStatus())
                .setResponseHeaders(source.getResponseHeaders())
                .setResponseBody(source.getResponseBody());
    }
}
