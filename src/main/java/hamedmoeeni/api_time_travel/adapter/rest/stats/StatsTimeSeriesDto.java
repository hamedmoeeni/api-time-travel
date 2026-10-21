package hamedmoeeni.api_time_travel.adapter.rest.stats;

import lombok.Data;

@Data
class StatsTimeSeriesDto {
    private String label;
    private Long[] time;
    private Long[] value;
}
