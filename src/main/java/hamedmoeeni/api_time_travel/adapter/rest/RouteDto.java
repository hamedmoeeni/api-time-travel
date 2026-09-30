package hamedmoeeni.api_time_travel.adapter.rest;

import lombok.Data;

@Data
public class RouteDto {
    private String id;
    private Integer priority;
    private String sourcePath;
    private String sourceHost;
    private String method;
    private String destinationHostUri;
}
