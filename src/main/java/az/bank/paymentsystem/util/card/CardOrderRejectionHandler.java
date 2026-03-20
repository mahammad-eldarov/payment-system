package az.bank.paymentsystem.util.card;

import az.bank.paymentsystem.entity.CardOrderEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.OrderStatus;
import az.bank.paymentsystem.enums.OrderType;
import az.bank.paymentsystem.exception.ExceptionResponse;
import az.bank.paymentsystem.exception.MultiValidationException;
import az.bank.paymentsystem.repository.CardOrderRepository;
import az.bank.paymentsystem.service.NotificationService;
import az.bank.paymentsystem.service.OrderRateLimitService;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardOrderRejectionHandler {

    private final OrderRateLimitService orderRateLimitService;
    private final CardOrderRepository cardOrderRepository;
    private final NotificationService notificationService;

    public void handleRejection(CardOrderEntity orderEntity,
                                CustomerEntity customer,
                                MultiValidationException ex) {
        orderEntity.setStatus(OrderStatus.REJECTED);
        String reason = ex.getErrors().stream()
                        .map(ExceptionResponse::getMessage)
                        .collect(Collectors.joining(", "));
        orderEntity.setRejectionReason(reason);
        orderRateLimitService.handleRejection(customer, OrderType.CARD);
        cardOrderRepository.save(orderEntity);
        notificationService.send(customer,
                "Your card order request has been rejected. Reason: " + reason);
    }

//    public void handleRejection(CardOrderEntity orderEntity,
//                                CustomerEntity customer,
//                                MultiValidationException ex) {
//        orderEntity.setStatus(OrderStatus.REJECTED);
//        orderEntity.setRejectionReason(
//                ex.getErrors().stream()
//                        .map(ExceptionResponse::getMessage)
//                        .collect(Collectors.joining(", "))
//        );
//        orderRateLimitService.handleRejection(customer, OrderType.CARD);
//        cardOrderRepository.save(orderEntity);
//        notificationService.send(customer,
//                "Your card order request has been rejected. Reason: " + reason);
//    }
}
