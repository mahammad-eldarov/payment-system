package az.bank.paymentsystem.util.payment;

import az.bank.paymentsystem.entity.ExternalPartyEntity;
import az.bank.paymentsystem.entity.TinEntity;
import az.bank.paymentsystem.enums.CardStatus;
import az.bank.paymentsystem.enums.TinStatus;
import az.bank.paymentsystem.enums.CustomerStatus;
import az.bank.paymentsystem.repository.ExternalPartyRepository;
import az.bank.paymentsystem.util.shared.CurrencyConverter;
import az.bank.paymentsystem.util.shared.MessageUtil;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.config.BankConfig;
import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.entity.PaymentEntity;
import az.bank.paymentsystem.enums.Currency;
import az.bank.paymentsystem.enums.PaymentSourceType;
import az.bank.paymentsystem.exception.ExceptionResponse;
import az.bank.paymentsystem.repository.CardRepository;
import az.bank.paymentsystem.repository.TinRepository;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaymentSourceResolver {
    private final CardRepository cardRepository;
    private final TinRepository tinRepository;
    private final BankConfig bankConfig;
    private final CurrencyConverter currencyConverter;
    private final ExternalPartyRepository externalPartyRepository;
    private final MessageSource messageSource;
    private final MessageUtil messageUtil;


    public void fromCheckCard(PaymentEntity payment, Integer customerId,
                               String fromPan, List<ExceptionResponse> errors) {
        Locale locale = messageUtil.resolveLocale(payment.getCustomer());

        CardEntity card = cardRepository.findByPanAndIsVisibleTrue(fromPan).orElse(null);
        if (card == null) {
            errors.add(new ExceptionResponse(404, messageSource.getMessage("paymentSourceResolver.fromCheckCard.cardNotFound",null, locale), LocalDateTime.now()));
            return;
        }
        if (!card.getCustomer().getId().equals(customerId)) {
            errors.add(new ExceptionResponse(403, messageSource.getMessage("paymentSourceResolver.fromCheckCard.cardNotBelong",null,locale), LocalDateTime.now()));
            return;
        }
        if (card.getStatus() == CardStatus.SUSPICIOUS) {
            errors.add(new ExceptionResponse(403, messageSource.getMessage("paymentSourceResolver.fromCheckCard.cardSuspended",null,locale), LocalDateTime.now()));
            return;
        }
        if (card.getStatus() == CardStatus.EXPIRED) {
            errors.add(new ExceptionResponse(400, messageSource.getMessage("paymentSourceResolver.fromCheckCard.cardExpired",null,locale), LocalDateTime.now()));
            return;
        }
        if (payment.getCustomer().getStatus() == CustomerStatus.SUSPICIOUS) {
            errors.add(new ExceptionResponse(403, messageSource.getMessage("paymentSourceResolver.fromCheckCard.fromCheckTin.customerSuspended",null,locale), LocalDateTime.now()));
            return;
        }

        boolean cardHasSufficientBalance = card.getBalance().compareTo(payment.getAmount()) >= 0;
        boolean cardAboveMinBalance = card.getBalance().compareTo(bankConfig.getCard().getMinBalance()) >= 0;

        if (cardHasSufficientBalance && cardAboveMinBalance) {
            payment.setFromCard(card);
            payment.setCurrency(card.getCurrency());
        } else {
            fallbackToTin(payment, customerId, card.getCurrency(), errors);
        }
    }

    public void fromCheckTin(PaymentEntity payment, Integer customerId,
                                  String fromTinNumber, List<ExceptionResponse> errors) {
        Locale locale = messageUtil.resolveLocale(payment.getCustomer());


        TinEntity tin = tinRepository
                .findByTinNumberAndIsVisibleTrue(fromTinNumber).orElse(null);
        if (tin == null) {
            errors.add(new ExceptionResponse(404, messageSource.getMessage("paymentSourceResolver.fromCheckTin.tinNotFound",null,locale), LocalDateTime.now()));
            return;
        }
        if (!tin.getCustomer().getId().equals(customerId)) {
            errors.add(new ExceptionResponse(403, messageSource.getMessage("paymentSourceResolver.fromCheckTin.tinNotBelong",null,locale), LocalDateTime.now()));
            return;
        }
        if (tin.getStatus() == TinStatus.SUSPICIOUS) {
            errors.add(new ExceptionResponse(403, messageSource.getMessage("paymentSourceResolver.fromCheckTin.tinSuspended",null,locale), LocalDateTime.now()));
            return;
        }
        if (payment.getCustomer().getStatus() == CustomerStatus.SUSPICIOUS) {
            errors.add(new ExceptionResponse(403, messageSource.getMessage("paymentSourceResolver.fromCheckCard.fromCheckTin.customerSuspended",null,locale), LocalDateTime.now()));
            return;
        }
        if (tin.getStatus() == TinStatus.EXPIRED) {
            errors.add(new ExceptionResponse(403, messageSource.getMessage("paymentSourceResolver.fromCheckTin.tinExpired",null,locale), LocalDateTime.now()));
            return;
        }
        if (tin.getStatus() == TinStatus.CLOSED) {
            errors.add(new ExceptionResponse(403, messageSource.getMessage("paymentSourceResolver.fromCheckTin.tinClosed",null,locale), LocalDateTime.now()));
            return;
        }

        BigDecimal minBalanceInTinCurrency = currencyConverter.convertMinBalance(
                bankConfig.getTin().getMinBalance(),
                bankConfig.getTin().getMinBalanceCurrency(),
                tin.getCurrency());

        boolean tinHasSufficientBalance = tin.getBalance().compareTo(payment.getAmount()) >= 0;
        boolean tinAboveMinBalance = tin.getBalance().compareTo(minBalanceInTinCurrency) >= 0;

        if (tinHasSufficientBalance && tinAboveMinBalance) {
            payment.setFromTin(tin);
            payment.setCurrency(tin.getCurrency());
        } else {
            fallbackToCard(payment, customerId, errors);
        }
    }

    private void fallbackToTin(PaymentEntity payment, Integer customerId,
                                   Currency cardCurrency, List<ExceptionResponse> errors) {
        Locale locale = messageUtil.resolveLocale(payment.getCustomer());


        TinEntity tin = tinRepository
                .findSufficientTin(customerId, payment.getAmount()).orElse(null);
        if (tin == null) {
            errors.add(new ExceptionResponse(400, messageSource.getMessage("paymentSourceResolver.fallbackToTin.insufficientBalance",null,locale), LocalDateTime.now()));
            return;
        }

        BigDecimal minBalanceInTinCurrency = currencyConverter.convertMinBalance(
                bankConfig.getTin().getMinBalance(),
                bankConfig.getTin().getMinBalanceCurrency(),
                tin.getCurrency());

        BigDecimal amountInTinCurrency = currencyConverter.convert(
                payment.getAmount(), cardCurrency, tin.getCurrency());

        if (tin.getBalance().compareTo(amountInTinCurrency) < 0 ||
                tin.getBalance().compareTo(minBalanceInTinCurrency) < 0) {
            errors.add(new ExceptionResponse(400, messageSource.getMessage("paymentSourceResolver.fallbackToTin.insufficientBalance",null,locale), LocalDateTime.now()));
            return;
        }

        payment.setFromTin(tin);
        payment.setFromType(PaymentSourceType.TIN);
        payment.setCurrency(tin.getCurrency());
    }

    private void fallbackToCard(PaymentEntity payment, Integer customerId,
                                List<ExceptionResponse> errors) {
        Locale locale = messageUtil.resolveLocale(payment.getCustomer());


        CardEntity card = cardRepository.findSufficientCard(customerId, payment.getAmount()).orElse(null);
        if (card == null) {
            errors.add(new ExceptionResponse(400, messageSource.getMessage("paymentSourceResolver.fallbackToCard.insufficientBalance",null,locale), LocalDateTime.now()));
            return;
        }
        payment.setFromCard(card);
        payment.setFromType(PaymentSourceType.CARD);
        payment.setCurrency(card.getCurrency());
    }

    public void toCheckCard(PaymentEntity payment, String toPan, List<ExceptionResponse> errors) {
        if (isInternalCard(toPan)) {
            resolveInternalCard(payment, toPan, errors);
        } else {
            resolveExternalCard(payment, toPan, errors);
        }
    }

    private boolean isInternalCard(String pan) {
        return pan != null && pan.startsWith("9988");
    }

    private void resolveInternalCard(PaymentEntity payment, String toPan, List<ExceptionResponse> errors) {
        Locale locale = messageUtil.resolveLocale(payment.getCustomer());


        CardEntity card = cardRepository.findByPanAndIsVisibleTrue(toPan).orElse(null);
        if (card == null) {
            errors.add(new ExceptionResponse(404, messageSource.getMessage("paymentSourceResolver.isInternalCard.cardNotFound",null,locale), LocalDateTime.now()));
            return;
        }
        if (card.getStatus() == CardStatus.EXPIRED) {
            errors.add(new ExceptionResponse(400, messageSource.getMessage("paymentSourceResolver.isInternalCard.cardExpired",null,locale), LocalDateTime.now()));
            return;
        }
        if (card.getStatus() == CardStatus.CLOSED) {
            errors.add(new ExceptionResponse(403, messageSource.getMessage("paymentSourceResolver.isInternalCard.cardClosed",null,locale), LocalDateTime.now()));
            return;
        }
        if (card.getStatus() == CardStatus.SUSPICIOUS) {
            errors.add(new ExceptionResponse(403, messageSource.getMessage("paymentSourceResolver.isInternalCard.cardSuspended",null,locale), LocalDateTime.now()));
            return;
        }
        payment.setToCard(card);
    }

    private void resolveExternalCard(PaymentEntity payment, String toPan, List<ExceptionResponse> errors) {
        Locale locale = messageUtil.resolveLocale(payment.getCustomer());


        Optional<ExternalPartyEntity> external = externalPartyRepository.findByCardNumber(toPan);

        if (external.isEmpty()) {
            errors.add(new ExceptionResponse(404, messageSource.getMessage("paymentSourceResolver.resolveExternalCard.cardNotFound",null,locale), LocalDateTime.now()));
            return;
        }
        payment.setToExternalParty(external.get());
        payment.setToType(PaymentSourceType.EXTERNAL);
    }

    public void toCheckTin(PaymentEntity payment, String toTinNumber, List<ExceptionResponse> errors) {
        if (isInternalTin(toTinNumber)) {
            resolveInternalTin(payment, toTinNumber, errors);
        } else {
            resolveExternalTin(payment, toTinNumber, errors);
        }
    }

    private boolean isInternalTin(String tinNumber) {
        return tinNumber != null && tinNumber.startsWith("2211");
    }

    private void resolveInternalTin(PaymentEntity payment, String toTinNumber, List<ExceptionResponse> errors) {
        Locale locale = messageUtil.resolveLocale(payment.getCustomer());


        TinEntity tin = tinRepository
                .findByTinNumberAndIsVisibleTrue(toTinNumber).orElse(null);
        if (tin == null) {
            errors.add(new ExceptionResponse(404, messageSource.getMessage("paymentSourceResolver.resolveInternalTin.tinNotFound",null,locale), LocalDateTime.now()));
            return;
        }
        if (tin.getStatus() == TinStatus.EXPIRED) {
            errors.add(new ExceptionResponse(403, messageSource.getMessage("paymentSourceResolver.resolveInternalTin.tinExpired",null,locale), LocalDateTime.now()));
            return;
        }
        if (tin.getStatus() == TinStatus.CLOSED) {
            errors.add(new ExceptionResponse(403, messageSource.getMessage("paymentSourceResolver.resolveInternalTin.tinClosed",null,locale), LocalDateTime.now()));
            return;
        }
        if (tin.getStatus() == TinStatus.SUSPICIOUS) {
            errors.add(new ExceptionResponse(403, messageSource.getMessage("paymentSourceResolver.resolveInternalTin.tinSuspended",null,locale), LocalDateTime.now()));
            return;
        }
        payment.setToTin(tin);
    }

    private void resolveExternalTin(PaymentEntity payment, String toTinNumber, List<ExceptionResponse> errors) {
        Locale locale = messageUtil.resolveLocale(payment.getCustomer());
        Optional<ExternalPartyEntity> external = externalPartyRepository.findByTinNumber(toTinNumber);
        if (external.isEmpty()) {
            errors.add(new ExceptionResponse(404, messageSource.getMessage("paymentSourceResolver.resolveExternalTin.tinNotFound",null,locale), LocalDateTime.now()));
            return;
        }
        payment.setToExternalParty(external.get());
        payment.setToType(PaymentSourceType.EXTERNAL);
    }

}
