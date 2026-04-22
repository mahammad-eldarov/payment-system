package az.bank.paymentsystem.mapper;

import az.bank.paymentsystem.dto.response.TinOrderResponse;
import az.bank.paymentsystem.dto.response.TinResponse;
import az.bank.paymentsystem.entity.TinEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TinMapper {

    @Mapping(target = "transactions", ignore = true)
    TinResponse toResponse(TinEntity account);

    TinOrderResponse toOrderResponse(TinEntity account);

}
