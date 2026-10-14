package hamedmoeeni.api_time_travel.adapter.rest.recording;

import lombok.Data;

@Data
class RecordingDto {
    private String requestId;
    private String routeId;
    private Long timestamp;
    private String url;
    private String method;
    private String requestHeaders;
    private String requestBody;
    private Long duration;
    private String responseStatus;
    private String responseHeaders;
    private String responseBody;
}
