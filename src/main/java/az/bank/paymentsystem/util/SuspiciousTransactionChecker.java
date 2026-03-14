package az.bank.paymentsystem.util;

import java.math.BigDecimal;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.config.BankConfig;
import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.entity.CurrentAccountEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.entity.PaymentEntity;
import az.bank.paymentsystem.enums.CardStatus;
import az.bank.paymentsystem.enums.CurrentAccountStatus;
import az.bank.paymentsystem.enums.CustomerStatus;
import az.bank.paymentsystem.repository.CardRepository;
import az.bank.paymentsystem.repository.CurrentAccountRepository;
import az.bank.paymentsystem.repository.CustomerRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SuspiciousTransactionChecker {

    private final CardRepository cardRepository;
    private final CurrentAccountRepository currentAccountRepository;
    private final CustomerRepository customerRepository;
    private final BankConfig bankConfig;

    public void check(PaymentEntity payment) {
        String suspiciousCurrency = bankConfig.getTransaction().getSuspiciousCurrency();
        BigDecimal threshold = bankConfig.getTransaction().getSuspiciousThreshold();

        if (payment.getCurrency().name().equals(suspiciousCurrency)
                && payment.getAmount().compareTo(threshold) >= 0) {

            markCustomerSuspicious(payment.getCustomer());

            if (payment.getFromCard() != null) {
                markCardSuspicious(payment.getFromCard());
            }
            if (payment.getFromAccount() != null) {
                markAccountSuspicious(payment.getFromAccount());
            }
        }
    }

    private void markCustomerSuspicious(CustomerEntity customer) {
        customer.setStatus(CustomerStatus.SUSPICIOUS);
        customer.setUpdatedAt(Instant.now());
        customerRepository.save(customer);
    }

    private void markCardSuspicious(CardEntity card) {
        card.setStatus(CardStatus.SUSPICIOUS);
        card.setUpdatedAt(Instant.now());
        cardRepository.save(card);
    }

    private void markAccountSuspicious(CurrentAccountEntity account) {
        account.setStatus(CurrentAccountStatus.SUSPICIOUS);
        account.setUpdatedAt(Instant.now());
        currentAccountRepository.save(account);
    }
}
