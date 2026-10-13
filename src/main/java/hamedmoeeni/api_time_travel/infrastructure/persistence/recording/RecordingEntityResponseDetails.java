package hamedmoeeni.api_time_travel.infrastructure.persistence.recording;

import lombok.Data;

@Data
class RecordingEntityResponseDetails {
    private String requestId;
    private Long duration;
    private String responseStatus;
    private String responseHeaders;
    private String responseBody;
}
