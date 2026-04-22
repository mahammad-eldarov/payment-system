package az.bank.paymentsystem.util.tin;

import az.bank.paymentsystem.entity.TinOrderEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.OrderStatus;
import az.bank.paymentsystem.enums.OrderType;
import az.bank.paymentsystem.exception.ExceptionResponse;
import az.bank.paymentsystem.exception.MultiValidationException;
import az.bank.paymentsystem.repository.TinOrderRepository;
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
public class TinOrderRejectionHandler {
    private final OrderRateLimitService orderRateLimitService;
    private final TinOrderRepository tinOrderRepository;
    private final NotificationService notificationService;
    private final MessageSource messageSource;
    private final MessageUtil messageUtil;


    public void handleRejection(TinOrderEntity orderEntity,
                                CustomerEntity customer,
                                MultiValidationException ex) {
        Locale locale = messageUtil.resolveLocale(customer);
        orderEntity.setStatus(OrderStatus.REJECTED);

        String reason = ex.getErrors().stream()
                        .map(ExceptionResponse::getMessage)
                        .collect(Collectors.joining(", "));

        orderEntity.setRejectionReason(reason);
        orderRateLimitService.handleRejection(customer, OrderType.TIN);
        tinOrderRepository.save(orderEntity);
        notificationService.send(customer,
                messageSource.getMessage("tinOrderRejectionHandler.handleRejection.reason", null, locale) + reason);
    }


}
