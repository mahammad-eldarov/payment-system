package az.bank.paymentsystem.util.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.entity.CurrentAccountEntity;
import az.bank.paymentsystem.entity.PaymentEntity;
import az.bank.paymentsystem.enums.CardStatus;
import az.bank.paymentsystem.enums.CurrentAccountStatus;
import az.bank.paymentsystem.exception.ExceptionResponse;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentValidator {

    public void validate(PaymentEntity payment, List<ExceptionResponse> errors) {
        validateCard(payment, errors);
        validateAccount(payment, errors);
        checkSelfTransfer(payment, errors);
    }

    private void validateCard(PaymentEntity payment, List<ExceptionResponse> errors) {
        if (payment.getFromCard() != null) {
            CardEntity fromCard = payment.getFromCard();
            if (fromCard.getStatus() != CardStatus.ACTIVE) {
                errors.add(new ExceptionResponse(400, "Source card is: " + fromCard.getStatus(), LocalDateTime.now()));
            } else if (fromCard.getBalance().compareTo(payment.getAmount()) < 0) {
                errors.add(new ExceptionResponse(400, "Insufficient balance on source card", LocalDateTime.now()));
            }
        }
        if (payment.getToCard() != null) {
            CardEntity toCard = payment.getToCard();
            if (toCard.getStatus() != CardStatus.ACTIVE) {
                errors.add(new ExceptionResponse(400, "Destination card is: " + toCard.getStatus(), LocalDateTime.now()));
            }
        }
    }

    private void validateAccount(PaymentEntity payment, List<ExceptionResponse> errors) {
        if (payment.getFromAccount() != null) {
            CurrentAccountEntity fromAccount = payment.getFromAccount();
            if (fromAccount.getStatus() != CurrentAccountStatus.ACTIVE) {
                errors.add(new ExceptionResponse(400, "Source current account is: " + fromAccount.getStatus(), LocalDateTime.now()));
            } else if (fromAccount.getBalance().compareTo(payment.getAmount()) < 0) {
                errors.add(new ExceptionResponse(400, "Insufficient balance on source current account", LocalDateTime.now()));
            }
        }
        if (payment.getToAccount() != null) {
            CurrentAccountEntity toAccount = payment.getToAccount();
            if (toAccount.getStatus() != CurrentAccountStatus.ACTIVE) {
                errors.add(new ExceptionResponse(400, "Destination current account is: " + toAccount.getStatus(), LocalDateTime.now()));
            }
        }
    }

    public void checkSelfTransfer(PaymentEntity payment, List<ExceptionResponse> errors) {
        if (payment.getFromCard() != null && payment.getToCard() != null &&
                payment.getFromCard().getId().equals(payment.getToCard().getId())) {
            errors.add(new ExceptionResponse(400, "Cannot transfer to the same card", LocalDateTime.now()));
        }
        if (payment.getFromAccount() != null && payment.getToAccount() != null &&
                payment.getFromAccount().getId().equals(payment.getToAccount().getId())) {
            errors.add(new ExceptionResponse(400, "Cannot transfer to the same account", LocalDateTime.now()));
        }
    }

    public void validateAmount(BigDecimal amount, List<ExceptionResponse> errors) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add(new ExceptionResponse(400, "Amount must be greater than 0", LocalDateTime.now()));
        }
    }
}
