package hamedmoeeni.api_time_travel.usecase;

import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PageMapper {

    default <T> Page<T> mapFromSpringDataPage(org.springframework.data.domain.Page<T> page) {
        return new Page<>(page.getContent(), page.getTotalElements(), page.getPageable().getOffset());
    }
}
