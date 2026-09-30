package hamedmoeeni.api_time_travel.usecase.routing;

import lombok.Data;

@Data
public class Route {
    private String id;
    private Integer priority;
    private String sourcePath;
    private String sourceHost;
    private String method;
    private String destinationHostUri;
}
