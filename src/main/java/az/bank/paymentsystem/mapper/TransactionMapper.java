package az.bank.paymentsystem.mapper;

import az.bank.paymentsystem.dto.response.TransactionResponse;
import az.bank.paymentsystem.entity.TransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(target = "paymentId", source = "payment.id")
    @Mapping(target = "paidBy", expression = "java(getPaidBy(t))")
    @Mapping(target = "enrollTo", expression = "java(getEnrollTo(t))")
    @Mapping(target = "customerName", expression = "java(t.getCustomer().getName() + \" \" + t.getCustomer().getSurname())")
    TransactionResponse toResponse(TransactionEntity t);

    default String getPaidBy(TransactionEntity t) {
        if (t.getFromCard() != null) return t.getFromCard().getPan();
        if (t.getFromAccount() != null) return t.getFromAccount().getAccountNumber();
        return null;
    }

    default String getEnrollTo(TransactionEntity t) {
        if (t.getToCard() != null) return t.getToCard().getPan();
        if (t.getToAccount() != null) return t.getToAccount().getAccountNumber();
        return null;
    }
}
