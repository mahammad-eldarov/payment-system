package az.bank.paymentsystem.mapper;

import az.bank.paymentsystem.dto.response.PaymentResponse;
import az.bank.paymentsystem.entity.PaymentEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

    PaymentResponse toResponse(PaymentEntity payment);

}
