package hamedmoeeni.api_time_travel.domain.stats;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class Stats {
    private String requestId;
    private String routeId;
    private ZonedDateTime timestamp;
    private String url;
    private String method;
    private String requestHeaders;
    private String requestBody;
    private Long duration;
    private String responseStatus;
    private String responseHeaders;
    private String responseBody;
}
