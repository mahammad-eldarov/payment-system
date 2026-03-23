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
import az.bank.paymentsystem.util.shared.MessageUtil;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor

public class CardOrderRejectionHandler {

    private final OrderRateLimitService orderRateLimitService;
    private final CardOrderRepository cardOrderRepository;
    private final NotificationService notificationService;
    private final MessageSource messageSource;
    private final MessageUtil messageUtil;

    public void handleRejection(CardOrderEntity orderEntity,
                                CustomerEntity customer,
                                MultiValidationException ex) {

        Locale locale = messageUtil.resolveLocale(customer);
        orderEntity.setStatus(OrderStatus.REJECTED);
        String reason = ex.getErrors().stream()
                        .map(ExceptionResponse::getMessage)
                        .collect(Collectors.joining(", "));
        orderEntity.setRejectionReason(reason);
        orderRateLimitService.handleRejection(customer, OrderType.CARD);
        cardOrderRepository.save(orderEntity);
        notificationService.send(customer,
                messageSource.getMessage("cardOrderRejectionHandler.handleRejection.reason",new Object[]{reason},locale));
    }

}
