package hamedmoeeni.api_time_travel.usecase.routing;

import lombok.Data;

@Data
public class Route {
    private Integer priority;
    private String sourcePath;
    private String destinationUri;
}
