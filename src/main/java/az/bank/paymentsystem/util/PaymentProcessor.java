package az.bank.paymentsystem.util;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.entity.PaymentEntity;
import az.bank.paymentsystem.enums.PaymentStatus;
import az.bank.paymentsystem.enums.TransactionStatus;
import az.bank.paymentsystem.exception.ExceptionResponse;
import az.bank.paymentsystem.exception.MultiValidationException;
import az.bank.paymentsystem.exception.PaymentNotFoundException;
import az.bank.paymentsystem.repository.PaymentRepository;
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void process(Integer paymentId) {
        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found"));
        try {
            processPaymentLogic(payment);
            markSuccess(payment);
            transactionCreator.create(payment, TransactionStatus.SUCCESS);
        } catch (MultiValidationException e) {
            markFailed(payment, String.join(", ", e.getErrors().stream()
                    .map(ExceptionResponse::getMessage).toList()));
            transactionCreator.create(payment, TransactionStatus.FAILED);
        } catch (Exception e) {
            markFailed(payment, "Unexpected error: " + e.getMessage());
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
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setProcessedAt(Instant.now());
        payment.setUpdatedAt(Instant.now());
        payment.setFailureReason("");
    }

    private void markFailed(PaymentEntity payment, String reason) {
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(reason);
        payment.setProcessedAt(Instant.now());
        payment.setUpdatedAt(Instant.now());
    }
}
