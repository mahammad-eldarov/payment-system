package az.bank.paymentsystem.util.payment;

import az.bank.paymentsystem.entity.TinEntity;
import az.bank.paymentsystem.util.shared.MessageUtil;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.entity.PaymentEntity;
import az.bank.paymentsystem.enums.CardStatus;
import az.bank.paymentsystem.enums.TinStatus;
import az.bank.paymentsystem.exception.ExceptionResponse;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentValidator {

    private final MessageSource messageSource;
    private final MessageUtil messageUtil;

    public void validate(PaymentEntity payment, List<ExceptionResponse> errors) {
        validateCard(payment, errors);
        validateTin(payment, errors);
        checkSelfTransfer(payment, errors);
    }

    private void validateCard(PaymentEntity payment, List<ExceptionResponse> errors) {
        Locale locale = messageUtil.resolveLocale(payment.getCustomer());
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

    private void validateTin(PaymentEntity payment, List<ExceptionResponse> errors) {
        Locale locale = messageUtil.resolveLocale(payment.getCustomer());
        if (payment.getFromTin() != null) {
            TinEntity fromTin = payment.getFromTin();
            if (fromTin.getStatus() != TinStatus.ACTIVE) {
                errors.add(new ExceptionResponse(400, messageSource.getMessage("paymentValidator.validateTin.sourceTinStatus", new Object[]{fromTin.getStatus()},locale), LocalDateTime.now()));
            } else if (fromTin.getBalance().compareTo(payment.getAmount()) < 0) {
                errors.add(new ExceptionResponse(400, messageSource.getMessage("paymentValidator.validateTin.insufficientBalance",null,locale), LocalDateTime.now()));
            }
        }
        if (payment.getToTin() != null) {
            TinEntity toTin = payment.getToTin();
            if (toTin.getStatus() != TinStatus.ACTIVE) {
                errors.add(new ExceptionResponse(400, messageSource.getMessage("paymentValidator.validateTin.destinationTinStatus", new Object[]{toTin.getStatus()},locale), LocalDateTime.now()));
            }
        }
    }

    public void checkSelfTransfer(PaymentEntity payment, List<ExceptionResponse> errors) {
        Locale locale = messageUtil.resolveLocale(payment.getCustomer());
        if (payment.getFromCard() != null && payment.getToCard() != null &&
                payment.getFromCard().getId().equals(payment.getToCard().getId())) {
            errors.add(new ExceptionResponse(400, messageSource.getMessage("paymentValidator.checkSelfTransfer.sameCard",null,locale), LocalDateTime.now()));
        }
        if (payment.getFromTin() != null && payment.getToTin() != null &&
                payment.getFromTin().getId().equals(payment.getToTin().getId())) {
            errors.add(new ExceptionResponse(400, messageSource.getMessage("paymentValidator.checkSelfTransfer.sameTin",null,locale), LocalDateTime.now()));
        }
    }

    public void validateAmount(BigDecimal amount, List<ExceptionResponse> errors) {
        Locale locale = LocaleContextHolder.getLocale();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add(new ExceptionResponse(400, messageSource.getMessage("paymentValidator.validateAmount",null,locale), LocalDateTime.now()));
        }
    }
}
