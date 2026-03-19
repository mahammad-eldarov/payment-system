package az.bank.paymentsystem.util.card;

import az.bank.paymentsystem.entity.CardOrderEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.OrderStatus;
import az.bank.paymentsystem.enums.OrderType;
import az.bank.paymentsystem.exception.ExceptionResponse;
import az.bank.paymentsystem.exception.MultiValidationException;
import az.bank.paymentsystem.repository.CardOrderRepository;
import az.bank.paymentsystem.service.OrderRateLimitService;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardOrderRejectionHandler {

    private final OrderRateLimitService orderRateLimitService;
    private final CardOrderRepository cardOrderRepository;

    public void handleRejection(CardOrderEntity orderEntity,
                                CustomerEntity customer,
                                MultiValidationException ex) {
        orderEntity.setStatus(OrderStatus.REJECTED);
        orderEntity.setRejectionReason(
                ex.getErrors().stream()
                        .map(ExceptionResponse::getMessage)
                        .collect(Collectors.joining(", "))
        );
        orderRateLimitService.handleRejection(customer, OrderType.CARD);
        cardOrderRepository.save(orderEntity);
    }
}
