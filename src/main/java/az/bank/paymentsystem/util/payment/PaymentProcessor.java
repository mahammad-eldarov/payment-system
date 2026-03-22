package az.bank.paymentsystem.util.payment;

//import az.bank.paymentsystem.service.EntityFinderService;
import az.bank.paymentsystem.service.NotificationService;
import az.bank.paymentsystem.util.shared.BalanceUpdater;
import az.bank.paymentsystem.util.shared.SuspiciousTransactionChecker;
import az.bank.paymentsystem.util.shared.TransactionCreator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.entity.PaymentEntity;
import az.bank.paymentsystem.enums.PaymentStatus;
import az.bank.paymentsystem.enums.TransactionStatus;
import az.bank.paymentsystem.exception.ExceptionResponse;
import az.bank.paymentsystem.exception.MultiValidationException;
import az.bank.paymentsystem.exception.PaymentNotFoundException;
import az.bank.paymentsystem.repository.PaymentRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PaymentProcessor {

    private final PaymentValidator paymentValidator;
    private final BalanceUpdater balanceUpdater;
    private final SuspiciousTransactionChecker suspiciousTransactionChecker;
    private final TransactionCreator transactionCreator;
    private final PaymentRepository paymentRepository;
    private final NotificationService notificationService;
    private final MessageSource messageSource;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process(Integer paymentId) {
        Locale locale = LocaleContextHolder.getLocale();
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException(messageSource.getMessage("paymentProcessor.process.paymentNotFound", null, locale)));
        try {
            processPaymentLogic(payment);
            markSuccess(payment);
            transactionCreator.create(payment, TransactionStatus.SUCCESS);
        } catch (MultiValidationException e) {
            markFailed(payment, String.join(", ", e.getErrors().stream()
                    .map(ExceptionResponse::getMessage).toList()));
            transactionCreator.create(payment, TransactionStatus.FAILED);
        } catch (Exception e) {
            markFailed(payment, messageSource.getMessage("paymentProcessor.process.unexpectedError",null, locale) + e.getMessage());
            transactionCreator.create(payment, TransactionStatus.FAILED);
        }
        paymentRepository.save(payment);
    }

    private void processPaymentLogic(PaymentEntity payment) {
        List<ExceptionResponse> errors = new ArrayList<>();
        paymentValidator.validate(payment, errors);
        if (!errors.isEmpty()) throw new MultiValidationException(errors);

        balanceUpdater.withdraw(payment);
        balanceUpdater.deposit(payment);
        suspiciousTransactionChecker.check(payment);
    }

    private void markSuccess(PaymentEntity payment) {
        Locale locale = LocaleContextHolder.getLocale();
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setProcessedAt(Instant.now());
        payment.setUpdatedAt(Instant.now());
        payment.setFailureReason("");
        notificationService.send(payment.getCustomer(),
                messageSource.getMessage("paymentProcessor.markSuccess",
                        new Object[]{payment.getAmount(), payment.getCurrency()}, locale));
    }

    private void markFailed(PaymentEntity payment, String reason) {
        Locale locale = LocaleContextHolder.getLocale();
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(reason);
        payment.setProcessedAt(Instant.now());
        payment.setUpdatedAt(Instant.now());
        notificationService.send(payment.getCustomer(),
                messageSource.getMessage("paymentProcessor.markFailed",new Object[]{payment.getAmount(), payment.getCurrency()}, locale)
                        + reason);
    }
}
