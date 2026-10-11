package hamedmoeeni.api_time_travel.infrastructure.persistence.stats;

import lombok.Data;

@Data
public class StatsEntity {
    private String requestId;
    private String routeId;
    private String utcTimestamp;
    private String url;
    private String method;
    private String requestHeaders;
    private String requestBody;
    private Long duration;
    private String responseStatus;
    private String responseHeaders;
    private String responseBody;
}
