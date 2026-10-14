package hamedmoeeni.api_time_travel.adapter.rest.recording;

import hamedmoeeni.api_time_travel.domain.recording.Recording;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

@Mapper(componentModel = "spring")
interface RecordingDtoMapper {

    @Named("zonedDataTimeToEpochMilli")
    static Long zonedDataTimeToUtcString(ZonedDateTime zonedDateTime) {
        return zonedDateTime == null ? null : zonedDateTime.toInstant().toEpochMilli();
    }

    @Named("timestampToZonedDateTime")
    static ZonedDateTime timestampToZonedDateTime(Long timestamp) {
        return timestamp == null ? null : ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneOffset.UTC);
    }

    @Mapping(source = "timestamp", target = "timestamp", qualifiedByName = "zonedDataTimeToEpochMilli")
    RecordingDto toRecordingDto(Recording record);
}
