package az.bank.paymentsystem.util;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.entity.CurrentAccountEntity;
import az.bank.paymentsystem.entity.PaymentEntity;
import az.bank.paymentsystem.enums.Currency;
import az.bank.paymentsystem.repository.CardRepository;
import az.bank.paymentsystem.repository.CurrentAccountRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BalanceUpdater {

    private final CardRepository cardRepository;
    private final CurrentAccountRepository currentAccountRepository;
    private final CurrencyConverter currencyConverter;

    public void withdraw(PaymentEntity payment) {
        switch (payment.getFromType()) {
            case CARD -> withdrawFromCard(payment.getFromCard(), payment.getAmount());
            case CURRENT_ACCOUNT -> withdrawFromAccount(payment.getFromAccount(), payment.getAmount());
        }
    }

    public void deposit(PaymentEntity payment) {
        switch (payment.getToType()) {
            case CARD -> depositToCard(payment.getToCard(), payment.getAmount(), payment.getCurrency());
            case CURRENT_ACCOUNT -> depositToAccount(payment.getToAccount(), payment.getAmount(), payment.getCurrency());
        }
    }

    private void withdrawFromCard(CardEntity card, BigDecimal amount) {
        card.setBalance(card.getBalance().subtract(amount));
        card.setUpdatedAt(Instant.now());
        cardRepository.save(card);
    }

    private void withdrawFromAccount(CurrentAccountEntity account, BigDecimal amount) {
        account.setBalance(account.getBalance().subtract(amount));
        account.setUpdatedAt(Instant.now());
        currentAccountRepository.save(account);
    }

    private void depositToCard(CardEntity card, BigDecimal amount, Currency sourceCurrency) {
        BigDecimal converted = currencyConverter.convert(amount, sourceCurrency, card.getCurrency());
        card.setBalance(card.getBalance().add(converted));
        card.setUpdatedAt(Instant.now());
        cardRepository.save(card);
    }

    private void depositToAccount(CurrentAccountEntity account, BigDecimal amount, Currency sourceCurrency) {
        BigDecimal converted = currencyConverter.convert(amount, sourceCurrency, account.getCurrency());
        account.setBalance(account.getBalance().add(converted));
        account.setUpdatedAt(Instant.now());
        currentAccountRepository.save(account);
    }
}