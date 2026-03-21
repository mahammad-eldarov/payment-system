package az.bank.paymentsystem.mapper;

import az.bank.paymentsystem.dto.response.CustomerShortResponse;
import az.bank.paymentsystem.dto.response.CustomerResponse;
import az.bank.paymentsystem.entity.CustomerEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "pin", ignore = true)
    @Mapping(target = "cardResponse", ignore = true)
    @Mapping(target = "currentAccountResponse", ignore = true)
    @Mapping(target = "cardMessage", ignore = true)
    @Mapping(target = "accountMessage", ignore = true)
    CustomerResponse toResponse(CustomerEntity customer);


    CustomerShortResponse toShortResponse(CustomerEntity customer);

}
