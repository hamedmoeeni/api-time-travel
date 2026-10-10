package hamedmoeeni.api_time_travel.adapter.rest.routing;

import hamedmoeeni.api_time_travel.domain.routing.Route;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RouteDtoMapper {

    RouteDto toDto(Route route);

    Route fromDto(RouteDto routeDto);
}
