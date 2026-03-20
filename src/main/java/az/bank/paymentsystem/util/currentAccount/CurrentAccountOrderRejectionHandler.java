package az.bank.paymentsystem.util.currentAccount;

import az.bank.paymentsystem.entity.CurrentAccountOrderEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.OrderStatus;
import az.bank.paymentsystem.enums.OrderType;
import az.bank.paymentsystem.exception.ExceptionResponse;
import az.bank.paymentsystem.exception.MultiValidationException;
import az.bank.paymentsystem.repository.CurrentAccountOrderRepository;
import az.bank.paymentsystem.service.NotificationService;
import az.bank.paymentsystem.service.OrderRateLimitService;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentAccountOrderRejectionHandler {
    private final OrderRateLimitService orderRateLimitService;
    private final CurrentAccountOrderRepository currentAccountOrderRepository;
    private final NotificationService notificationService;


    public void handleRejection(CurrentAccountOrderEntity orderEntity,
                                CustomerEntity customer,
                                MultiValidationException ex) {
        orderEntity.setStatus(OrderStatus.REJECTED);

        String reason = ex.getErrors().stream()
                        .map(ExceptionResponse::getMessage)
                        .collect(Collectors.joining(", "));

        orderEntity.setRejectionReason(reason);
        orderRateLimitService.handleRejection(customer, OrderType.CURRENT_ACCOUNT);
        currentAccountOrderRepository.save(orderEntity);
        notificationService.send(customer,
                "Your current account order request has been rejected. Reason: " + reason);
    }

//    public void handleRejection(CurrentAccountOrderEntity orderEntity,
//                                CustomerEntity customer,
//                                MultiValidationException ex) {
//        orderEntity.setStatus(OrderStatus.REJECTED);
//        orderEntity.setRejectionReason(
//                ex.getErrors().stream()
//                        .map(ExceptionResponse::getMessage)
//                        .collect(Collectors.joining(", "))
//        );
//        orderRateLimitService.handleRejection(customer, OrderType.CURRENT_ACCOUNT);
//        currentAccountOrderRepository.save(orderEntity);
//    }


}
