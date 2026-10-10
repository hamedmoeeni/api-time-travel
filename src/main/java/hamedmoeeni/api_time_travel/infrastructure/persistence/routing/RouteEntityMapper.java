package hamedmoeeni.api_time_travel.infrastructure.persistence.routing;

import hamedmoeeni.api_time_travel.domain.routing.Route;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
interface RouteEntityMapper {

    Route fromRouteEntity(RouteEntity routeEntity);

    RouteEntity toRouteEntity(Route route);
}
