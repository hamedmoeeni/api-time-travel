package hamedmoeeni.api_time_travel.infrastructure.persistence.recording;

import lombok.Data;

@Data
class RecordingEntityRequestDetails {
    private String requestId;
    private String routeId;
    private Long timestamp;
    private String url;
    private String method;
    private String requestHeaders;
    private String requestBody;
}
