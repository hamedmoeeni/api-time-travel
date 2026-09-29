package hamedmoeeni.api_time_travel.adapter.rest;

import lombok.Data;

@Data
public class RouteDto {
    private Integer priority;
    private String sourcePath;
    private String destinationUri;
}
