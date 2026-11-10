package hamedmoeeni.api_time_travel.adapter.rest.recording;

import lombok.Data;

@Data
class CriteriaDto {
    private String[] requestId;
    private String[] routeId;
    private Integer[] responseStatus;
    private String[] method;
    private String fromTimestamp;
    private String toTimestamp;
    private String sortBy;
    private String sortOrder;
    private Long pageNumber;
    private Long pageSize;
}
