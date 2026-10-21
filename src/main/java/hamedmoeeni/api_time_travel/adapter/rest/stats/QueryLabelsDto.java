package hamedmoeeni.api_time_travel.adapter.rest.stats;

import lombok.Data;

import java.util.List;

@Data
class QueryLabelsDto {
    private List<String> methods;
    private List<String> status;
    private List<String> routeIds;
    private List<String> urls;
}
