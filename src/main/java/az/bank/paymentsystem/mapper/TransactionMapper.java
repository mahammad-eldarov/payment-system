package az.bank.paymentsystem.mapper;

import az.bank.paymentsystem.dto.response.TransactionResponse;
import az.bank.paymentsystem.entity.TransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "paymentId", source = "payment.id")
    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "fromCardId", source = "fromCard.id")
    @Mapping(target = "fromAccountId", source = "fromAccount.id")
    @Mapping(target = "toCardId", source = "toCard.id")
    @Mapping(target = "toAccountId", source = "toAccount.id")
    TransactionResponse toResponse(TransactionEntity transaction);
}
