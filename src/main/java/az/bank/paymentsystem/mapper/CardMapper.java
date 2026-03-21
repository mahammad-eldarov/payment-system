package az.bank.paymentsystem.mapper;

import az.bank.paymentsystem.dto.response.CardForCustomerResponse;
import az.bank.paymentsystem.dto.response.CardOrderResponse;
import az.bank.paymentsystem.dto.response.CardResponse;
import az.bank.paymentsystem.entity.CardEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardMapper {

    @Mapping(target = "cvv", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "transactions", ignore = true)
    CardResponse toResponse(CardEntity card);


    CardOrderResponse toOrderResponse(CardEntity card);

    CardForCustomerResponse toSummary(CardEntity card);
}
