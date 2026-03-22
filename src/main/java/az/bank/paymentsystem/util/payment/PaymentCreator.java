package az.bank.paymentsystem.util.payment;

//import az.bank.paymentsystem.service.EntityFinderService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.entity.PaymentEntity;
import az.bank.paymentsystem.enums.PaymentSourceType;
import az.bank.paymentsystem.enums.PaymentStatus;
import az.bank.paymentsystem.exception.ExceptionResponse;
import az.bank.paymentsystem.exception.MultiValidationException;
import az.bank.paymentsystem.repository.CustomerRepository;
import az.bank.paymentsystem.repository.PaymentRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentCreator {

    private final PaymentRepository paymentRepository;
    private final CustomerRepository customerRepository;
    private final MessageSource messageSource;

    public PaymentEntity buildPayment(Integer customerId, BigDecimal amount,
                                      PaymentSourceType fromType, PaymentSourceType toType) {
        Locale locale = LocaleContextHolder.getLocale();
        if (paymentRepository.existsByCustomerIdAndScheduledDateAndStatus(
                customerId, LocalDate.now(), PaymentStatus.PENDING)) {
            throw new MultiValidationException(List.of(
                    new ExceptionResponse(400, messageSource.getMessage("paymentCreator.buildPayment.pendingPayment",null,locale), LocalDateTime.now())));
        }

        CustomerEntity customer = customerRepository.findByIdAndIsVisibleTrue(customerId)
                .orElseThrow(() -> new MultiValidationException(List.of(
                        new ExceptionResponse(404, messageSource.getMessage("paymentCreator.buildPayment.customerNotFound",null,locale), LocalDateTime.now()))));

        BigDecimal safeAmount = (amount != null && amount.compareTo(BigDecimal.ZERO) > 0)
                ? amount : BigDecimal.ZERO;

        PaymentEntity payment = new PaymentEntity();
        payment.setAmount(safeAmount);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setScheduledDate(LocalDate.now());
        payment.setFromType(fromType);
        payment.setToType(toType);
        payment.setCustomer(customer);
        payment.setCreatedAt(Instant.now());
        return payment;
    }
}
