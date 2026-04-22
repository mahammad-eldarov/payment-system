package az.bank.paymentsystem.util.shared;

import az.bank.paymentsystem.exception.TinNotFoundException;
import az.bank.paymentsystem.exception.CardNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.entity.TinEntity;
import az.bank.paymentsystem.entity.PaymentEntity;
import az.bank.paymentsystem.enums.Currency;
import az.bank.paymentsystem.repository.CardRepository;
import az.bank.paymentsystem.repository.TinRepository;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BalanceUpdater {

    private final CardRepository cardRepository;
    private final TinRepository tinRepository;
    private final CurrencyConverter currencyConverter;
    private final MessageUtil messageUtil;
    private final MessageSource messageSource;

    public void withdraw(PaymentEntity payment) {
        switch (payment.getFromType()) {
            case CARD -> withdrawFromCard(payment.getFromCard(), payment.getAmount());
            case TIN -> withdrawFromTin(payment.getFromTin(), payment.getAmount());
        }
    }

    public void deposit(PaymentEntity payment) {
        switch (payment.getToType()) {
            case CARD -> depositToCard(payment.getToCard(), payment.getAmount(), payment.getCurrency());
            case TIN ->
                    depositToTin(payment.getToTin(), payment.getAmount(), payment.getCurrency());
        }
    }

    private void withdrawFromCard(CardEntity card, BigDecimal amount) {
        Locale locale = messageUtil.resolveLocale(card.getCustomer());
        CardEntity lockedCard = cardRepository.findByIdWithLock(card.getId())
                .orElseThrow(() -> new CardNotFoundException(
                        messageSource.getMessage("balanceUpdater.card",
                                new Object[]{card.getId()}, locale)));
        lockedCard.setBalance(lockedCard.getBalance().subtract(amount));
        lockedCard.setUpdatedAt(Instant.now());
        cardRepository.save(lockedCard);
    }

    private void withdrawFromTin(TinEntity tin, BigDecimal amount) {
        Locale locale = messageUtil.resolveLocale(tin.getCustomer());
        TinEntity lockedTin = tinRepository.findByIdWithLock(tin.getId())
                .orElseThrow(() -> new TinNotFoundException(
                        messageSource.getMessage("balanceUpdater.tin",
                                new Object[]{tin.getId()}, locale)));
        lockedTin.setBalance(lockedTin.getBalance().subtract(amount));
        lockedTin.setUpdatedAt(Instant.now());
        tinRepository.save(lockedTin);
    }

    private void depositToCard(CardEntity card, BigDecimal amount, Currency sourceCurrency) {
        Locale locale = messageUtil.resolveLocale(card.getCustomer());
        CardEntity lockedCard = cardRepository.findByIdWithLock(card.getId())
                .orElseThrow(() -> new CardNotFoundException(
                        messageSource.getMessage("balanceUpdater.card",
                                new Object[]{card.getId()}, locale)));
        BigDecimal converted = currencyConverter.convert(amount, sourceCurrency, lockedCard.getCurrency());
        lockedCard.setBalance(lockedCard.getBalance().add(converted));
        lockedCard.setUpdatedAt(Instant.now());
        cardRepository.save(lockedCard);
    }

    private void depositToTin(TinEntity tin, BigDecimal amount, Currency sourceCurrency) {
        Locale locale = messageUtil.resolveLocale(tin.getCustomer());
        TinEntity lockedTin = tinRepository.findByIdWithLock(tin.getId())
                .orElseThrow(() -> new TinNotFoundException(
                        messageSource.getMessage("balanceUpdater.tin",
                                new Object[]{tin.getId()}, locale)));
        BigDecimal converted = currencyConverter.convert(amount, sourceCurrency, lockedTin.getCurrency());
        lockedTin.setBalance(lockedTin.getBalance().add(converted));
        lockedTin.setUpdatedAt(Instant.now());
        tinRepository.save(lockedTin);
    }

    public void refund(PaymentEntity payment) {
        switch (payment.getFromType()) {
            case CARD -> depositToCard(payment.getFromCard(), payment.getAmount(), payment.getCurrency());
            case TIN -> depositToTin(payment.getFromTin(), payment.getAmount(), payment.getCurrency());
        }
    }
}