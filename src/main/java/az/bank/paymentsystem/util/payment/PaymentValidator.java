package az.bank.paymentsystem.util.payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.entity.CurrentAccountEntity;
import az.bank.paymentsystem.entity.PaymentEntity;
import az.bank.paymentsystem.enums.CardStatus;
import az.bank.paymentsystem.enums.CurrentAccountStatus;
import az.bank.paymentsystem.exception.ExceptionResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentValidator {

    private final MessageSource messageSource;

    public void validate(PaymentEntity payment, List<ExceptionResponse> errors) {
        validateCard(payment, errors);
        validateAccount(payment, errors);
        checkSelfTransfer(payment, errors);
    }

    private void validateCard(PaymentEntity payment, List<ExceptionResponse> errors) {
        Locale locale =  LocaleContextHolder.getLocale();
        if (payment.getFromCard() != null) {
            CardEntity fromCard = payment.getFromCard();
            if (fromCard.getStatus() != CardStatus.ACTIVE) {
                errors.add(new ExceptionResponse(400, messageSource.getMessage("paymentValidator.validateCard.sourceCardStatus", new Object[] {fromCard.getStatus()}, locale), LocalDateTime.now()));
            } else if (fromCard.getBalance().compareTo(payment.getAmount()) < 0) {
                errors.add(new ExceptionResponse(400, messageSource.getMessage("paymentValidator.validateCard.insufficientBalance",null,locale), LocalDateTime.now()));
            }
        }
        if (payment.getToCard() != null) {
            CardEntity toCard = payment.getToCard();
            if (toCard.getStatus() != CardStatus.ACTIVE) {
                errors.add(new ExceptionResponse(400, messageSource.getMessage("paymentValidator.validateCard.destinationCardStatus", new Object[]{toCard.getStatus()},locale), LocalDateTime.now()));
            }
        }
    }

    private void validateAccount(PaymentEntity payment, List<ExceptionResponse> errors) {
        Locale locale =  LocaleContextHolder.getLocale();
        if (payment.getFromAccount() != null) {
            CurrentAccountEntity fromAccount = payment.getFromAccount();
            if (fromAccount.getStatus() != CurrentAccountStatus.ACTIVE) {
                errors.add(new ExceptionResponse(400, messageSource.getMessage("paymentValidator.validateAccount.sourceAccountStatus", new Object[]{fromAccount.getStatus()},locale), LocalDateTime.now()));
            } else if (fromAccount.getBalance().compareTo(payment.getAmount()) < 0) {
                errors.add(new ExceptionResponse(400, messageSource.getMessage("paymentValidator.validateAccount.insufficientBalance",null,locale), LocalDateTime.now()));
            }
        }
        if (payment.getToAccount() != null) {
            CurrentAccountEntity toAccount = payment.getToAccount();
            if (toAccount.getStatus() != CurrentAccountStatus.ACTIVE) {
                errors.add(new ExceptionResponse(400, messageSource.getMessage("paymentValidator.validateAccount.destinationAccountStatus", new Object[]{toAccount.getStatus()},locale), LocalDateTime.now()));
            }
        }
    }

    public void checkSelfTransfer(PaymentEntity payment, List<ExceptionResponse> errors) {
        Locale locale = LocaleContextHolder.getLocale();
        if (payment.getFromCard() != null && payment.getToCard() != null &&
                payment.getFromCard().getId().equals(payment.getToCard().getId())) {
            errors.add(new ExceptionResponse(400, messageSource.getMessage("paymentValidator.checkSelfTransfer.sameCard",null,locale), LocalDateTime.now()));
        }
        if (payment.getFromAccount() != null && payment.getToAccount() != null &&
                payment.getFromAccount().getId().equals(payment.getToAccount().getId())) {
            errors.add(new ExceptionResponse(400, messageSource.getMessage("paymentValidator.checkSelfTransfer.sameAccount",null,locale), LocalDateTime.now()));
        }
    }

    public void validateAmount(BigDecimal amount, List<ExceptionResponse> errors) {
        Locale locale = LocaleContextHolder.getLocale();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add(new ExceptionResponse(400, messageSource.getMessage("paymentValidator.validateAmount",null,locale), LocalDateTime.now()));
        }
    }
}
