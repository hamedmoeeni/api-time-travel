package hamedmoeeni.api_time_travel.adapter.persistence.routing;

import lombok.Data;

@Data
public class RouteEntity {
    private String id;
    private Integer priority;
    private String sourcePath;
    private String sourceHost;
    private String method;
    private String destinationHostUri;
}
