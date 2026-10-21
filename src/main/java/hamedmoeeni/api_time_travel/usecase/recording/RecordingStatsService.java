package hamedmoeeni.api_time_travel.usecase.recording;

import lombok.Data;
import reactor.core.publisher.Mono;

import java.util.List;

public interface RecordingStatsService {

    Mono<QueryLabels> getQueryLabels();

    Mono<List<RecordingStatsService.StatsTimeSeries>> queryTimeSeries(StatsQuery statsQuery);

    @Data
    class QueryLabels {
        private List<String> methods;
        private List<String> status;
        private List<String> routeIds;
        private List<String> urls;
    }

    @Data
    class StatsQuery {
        private OutputType outputType;
        private Long fromTimestamp;
        private Long toTimestamp;
        private List<String> methods;
        private List<String> routeIds;
        private List<String> urls;
        private List<String> status;
        private String groupBy;

        public enum OutputType{
            COUNT,
            AVG_RESPONSE_TIME
        }
    }

    @Data
    class StatsTimeSeries {
        private String label;
        private Long[] time;
        private Double[] value;
    }
}
