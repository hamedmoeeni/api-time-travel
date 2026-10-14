package hamedmoeeni.api_time_travel.adapter.rest.recording;

import lombok.Data;

@Data
class CriteriaDto {
    private String requestId;
    private String routeId;
    private String responseStatus;
    private String fromTimestamp;
    private String toTimestamp;
    private String sortBy;
    private String sortOrder;
    private Long page;
    private Long limit;
}
