package az.bank.paymentsystem.util.shared;

import az.bank.paymentsystem.repository.StatusAuditLogRepository;
import az.bank.paymentsystem.service.NotificationService;
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
    private final StatusAuditLogger statusAuditLogger;
    private final NotificationService notificationService;

    public void check(PaymentEntity payment) {
        if (isUnderThreshold(payment)) return;

        CustomerEntity customer = payment.getCustomer();
        if (customer.getStatus() == CustomerStatus.SUSPICIOUS) return;

        if (isAlreadySuspicious(customer.getId())) {
            markCustomerSuspicious(payment);
        } else {
            markSourceSuspicious(payment);
        }
    }

    private boolean isUnderThreshold(PaymentEntity payment) {
        return payment.getAmount().compareTo(bankConfig.getTransaction().getSuspiciousThreshold()) < 0;
    }

    private boolean isAlreadySuspicious(Integer customerId) {
        return cardRepository.existsByCustomerIdAndStatus(customerId, CardStatus.SUSPICIOUS)
                || currentAccountRepository.existsByCustomerIdAndStatus(customerId, CurrentAccountStatus.SUSPICIOUS);
    }

    private void markSourceSuspicious(PaymentEntity payment) {
        if (payment.getFromCard() != null) {
            CardEntity card = payment.getFromCard();
            statusAuditLogger.logCard(card, CardStatus.SUSPICIOUS.name(), "Suspicious transaction detected");
            card.setStatus(CardStatus.SUSPICIOUS);
            cardRepository.save(card);
            notificationService.send(card.getCustomer(),
                    "Your card ending in " + card.getPan().substring(card.getPan().length() - 4)
                            + " has been suspended due to suspicious activity.");
        } else if (payment.getFromAccount() != null) {
            CurrentAccountEntity account = payment.getFromAccount();
            statusAuditLogger.logAccount(account, CurrentAccountStatus.SUSPICIOUS.name(), "Suspicious transaction detected");
            account.setStatus(CurrentAccountStatus.SUSPICIOUS);
            currentAccountRepository.save(account);
            notificationService.send(account.getCustomer(),
                    "Your account " + account.getAccountNumber()
                            + " has been suspended due to suspicious activity.");
        }
    }

    private void markCustomerSuspicious(PaymentEntity payment) {
        CustomerEntity customer = payment.getCustomer();
        statusAuditLogger.logCustomer(customer, CustomerStatus.SUSPICIOUS.name(), "Second suspicious transaction detected");
        customer.setStatus(CustomerStatus.SUSPICIOUS);
        customerRepository.save(customer);
        notificationService.send(customer,
                "Your account has been suspended due to repeated suspicious activity.");

        if (payment.getFromCard() != null) {
            payment.getFromCard().setStatus(CardStatus.SUSPICIOUS);
            cardRepository.save(payment.getFromCard());
        } else if (payment.getFromAccount() != null) {
            payment.getFromAccount().setStatus(CurrentAccountStatus.SUSPICIOUS);
            currentAccountRepository.save(payment.getFromAccount());
        }
    }
}
