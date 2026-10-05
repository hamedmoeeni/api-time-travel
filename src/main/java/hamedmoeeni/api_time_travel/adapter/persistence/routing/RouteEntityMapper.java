package hamedmoeeni.api_time_travel.adapter.persistence.routing;

import hamedmoeeni.api_time_travel.usecase.routing.Route;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RouteEntityMapper {

    Route fromRouteEntity(RouteEntity routeEntity);

    RouteEntity toRouteEntity(Route route);
}
