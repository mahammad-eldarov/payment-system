package az.bank.paymentsystem.mapper;

import az.bank.paymentsystem.dto.response.CurrentAccountOrderResponse;
import az.bank.paymentsystem.dto.response.CurrentAccountResponse;
import az.bank.paymentsystem.entity.CurrentAccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CurrentAccountMapper {

    @Mapping(target = "transactions", ignore = true)
    CurrentAccountResponse toResponse(CurrentAccountEntity account);

    CurrentAccountOrderResponse toOrderResponse(CurrentAccountEntity account);

}
