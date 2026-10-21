package hamedmoeeni.api_time_travel.adapter.rest.stats;

import lombok.Data;

import java.util.List;

@Data
class StatsQueryDto {
    private String outputType;
    private Long fromTimestamp;
    private Long toTimestamp;
    private List<String> methods;
    private List<String> routeIds;
    private List<String> urls;
    private List<String> status;
    private String groupBy;
}
