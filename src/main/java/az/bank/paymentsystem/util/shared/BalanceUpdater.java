package az.bank.paymentsystem.util.shared;

import az.bank.paymentsystem.exception.AccountNotFoundException;
import az.bank.paymentsystem.exception.CardNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.entity.CurrentAccountEntity;
import az.bank.paymentsystem.entity.PaymentEntity;
import az.bank.paymentsystem.enums.Currency;
import az.bank.paymentsystem.repository.CardRepository;
import az.bank.paymentsystem.repository.CurrentAccountRepository;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BalanceUpdater {

    private final CardRepository cardRepository;
    private final CurrentAccountRepository currentAccountRepository;
    private final CurrencyConverter currencyConverter;
    private final MessageUtil messageUtil;
    private final MessageSource messageSource;

    public void withdraw(PaymentEntity payment) {
        switch (payment.getFromType()) {
            case CARD -> withdrawFromCard(payment.getFromCard(), payment.getAmount());
            case CURRENT_ACCOUNT -> withdrawFromAccount(payment.getFromAccount(), payment.getAmount());
        }
    }

    public void deposit(PaymentEntity payment) {
        switch (payment.getToType()) {
            case CARD -> depositToCard(payment.getToCard(), payment.getAmount(), payment.getCurrency());
            case CURRENT_ACCOUNT ->
                    depositToAccount(payment.getToAccount(), payment.getAmount(), payment.getCurrency());
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

    private void withdrawFromAccount(CurrentAccountEntity account, BigDecimal amount) {
        Locale locale = messageUtil.resolveLocale(account.getCustomer());
        CurrentAccountEntity lockedAccount = currentAccountRepository.findByIdWithLock(account.getId())
                .orElseThrow(() -> new AccountNotFoundException(
                        messageSource.getMessage("balanceUpdater.account",
                                new Object[]{account.getId()}, locale)));
        lockedAccount.setBalance(lockedAccount.getBalance().subtract(amount));
        lockedAccount.setUpdatedAt(Instant.now());
        currentAccountRepository.save(lockedAccount);
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

    private void depositToAccount(CurrentAccountEntity account, BigDecimal amount, Currency sourceCurrency) {
        Locale locale = messageUtil.resolveLocale(account.getCustomer());
        CurrentAccountEntity lockedAccount = currentAccountRepository.findByIdWithLock(account.getId())
                .orElseThrow(() -> new AccountNotFoundException(
                        messageSource.getMessage("balanceUpdater.account",
                                new Object[]{account.getId()}, locale)));
        BigDecimal converted = currencyConverter.convert(amount, sourceCurrency, lockedAccount.getCurrency());
        lockedAccount.setBalance(lockedAccount.getBalance().add(converted));
        lockedAccount.setUpdatedAt(Instant.now());
        currentAccountRepository.save(lockedAccount);
    }

    public void refund(PaymentEntity payment) {
        switch (payment.getFromType()) {
            case CARD -> depositToCard(payment.getFromCard(), payment.getAmount(), payment.getCurrency());
            case CURRENT_ACCOUNT -> depositToAccount(payment.getFromAccount(), payment.getAmount(), payment.getCurrency());
        }
    }
}