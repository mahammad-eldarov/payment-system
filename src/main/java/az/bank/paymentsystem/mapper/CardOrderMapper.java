package az.bank.paymentsystem.mapper;

import az.bank.paymentsystem.dto.response.CardOrderResponse;
import az.bank.paymentsystem.entity.CardOrderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CardOrderMapper {
    @Mapping(target = "id", source = "id")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "rejectionReason", source = "rejectionReason")
    @Mapping(target = "cardHolderName", source = "cardHolderName")
    @Mapping(target = "cardName", source = "cardName")
    @Mapping(target = "cardBrand", source = "cardBrand")
    @Mapping(target = "cardType", source = "cardType")
    @Mapping(target = "currency", source = "currency")
    @Mapping(target = "createdAt", source = "createdAt")
    CardOrderResponse toResponse(CardOrderEntity entity);
}
