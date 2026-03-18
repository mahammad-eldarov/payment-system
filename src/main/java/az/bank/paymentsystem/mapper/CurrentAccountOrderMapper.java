package az.bank.paymentsystem.mapper;

import az.bank.paymentsystem.dto.response.CurrentAccountOrderResponse;
import az.bank.paymentsystem.entity.CurrentAccountOrderEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CurrentAccountOrderMapper {
    CurrentAccountOrderResponse toResponse(CurrentAccountOrderEntity entity);
}
