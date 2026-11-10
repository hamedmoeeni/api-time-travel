package hamedmoeeni.api_time_travel.domain.recording;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class Recording implements Cloneable{
    private Type type;
    private String requestId;
    private String routeId;
    private ZonedDateTime timestamp;
    private String url;
    private String method;
    private String requestHeaders;
    private String requestBody;
    private Long duration;
    private Integer responseStatus;
    private String responseHeaders;
    private String responseBody;

    public enum Type{
        REQUEST,
        RESPONSE
    }

    @Override
    public Recording clone() throws CloneNotSupportedException {
        return (Recording) super.clone();
    }
}
