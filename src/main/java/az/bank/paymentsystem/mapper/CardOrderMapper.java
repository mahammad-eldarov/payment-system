package az.bank.paymentsystem.mapper;

import az.bank.paymentsystem.dto.response.CardOrderResponse;
import az.bank.paymentsystem.entity.CardOrderEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CardOrderMapper {
    CardOrderResponse toResponse(CardOrderEntity entity);
}
