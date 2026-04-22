package az.bank.paymentsystem.util.shared;

import az.bank.paymentsystem.service.NotificationService;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.config.BankConfig;
import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.entity.TinEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.entity.PaymentEntity;
import az.bank.paymentsystem.enums.CardStatus;
import az.bank.paymentsystem.enums.TinStatus;
import az.bank.paymentsystem.enums.CustomerStatus;
import az.bank.paymentsystem.repository.CardRepository;
import az.bank.paymentsystem.repository.TinRepository;
import az.bank.paymentsystem.repository.CustomerRepository;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SuspiciousTransactionChecker {

    private final CardRepository cardRepository;
    private final TinRepository tinRepository;
    private final CustomerRepository customerRepository;
    private final BankConfig bankConfig;
    private final StatusAuditLogger statusAuditLogger;
    private final NotificationService notificationService;
    private final FraudBlacklistChecker fraudBlacklistChecker;
    private final MessageSource messageSource;
    private final MessageUtil messageUtil;

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
                || tinRepository.existsByCustomerIdAndStatus(customerId, TinStatus.SUSPICIOUS);
    }

    private void markSourceSuspicious(PaymentEntity payment) {
        Locale locale = messageUtil.resolveLocale(payment.getCustomer());
        if (payment.getFromCard() != null) {
            CardEntity card = payment.getFromCard();
            statusAuditLogger.logCard(card, CardStatus.SUSPICIOUS.name(), messageSource.getMessage("suspiciousTransactionChecker.markSourceSuspicious.suspiciousTransaction", null, locale));
            card.setStatus(CardStatus.SUSPICIOUS);
            cardRepository.save(card);
            notificationService.send(card.getCustomer(),
                    messageSource.getMessage("suspiciousTransactionChecker.markSourceSuspicious.cardSuspiciousActivity",new Object[]{card.getPan().substring(card.getPan().length() - 4)},locale));
        } else if (payment.getFromTin() != null) {
            TinEntity tin = payment.getFromTin();
            statusAuditLogger.logTin(tin, TinStatus.SUSPICIOUS.name(), messageSource.getMessage("suspiciousTransactionChecker.markSourceSuspicious.suspiciousTransaction", null, locale));
            tin.setStatus(TinStatus.SUSPICIOUS);
            tinRepository.save(tin);
            notificationService.send(tin.getCustomer(),
                    messageSource.getMessage("suspiciousTransactionChecker.markSourceSuspicious.tinSuspiciousActivity", new Object[]{tin.getTinNumber()},locale));
        }
    }

    private void markCustomerSuspicious(PaymentEntity payment) {
        Locale locale = messageUtil.resolveLocale(payment.getCustomer());
        CustomerEntity customer = payment.getCustomer();
        statusAuditLogger.logCustomer(customer, CustomerStatus.SUSPICIOUS.name(), messageSource.getMessage("suspiciousTransactionChecker.markCustomerSuspicious.secondSuspiciousTransaction",null, locale));
        customer.setStatus(CustomerStatus.SUSPICIOUS);
        customerRepository.save(customer);
        fraudBlacklistChecker.addToBlacklist(customer, messageSource.getMessage("suspiciousTransactionChecker.markCustomerSuspicious.multipleSuspiciousTransaction",null, locale));
        notificationService.send(customer,
                messageSource.getMessage("suspiciousTransactionChecker.markCustomerSuspicious.profileSuspiciousActivity",null, locale));

        if (payment.getFromCard() != null) {
            payment.getFromCard().setStatus(CardStatus.SUSPICIOUS);
            cardRepository.save(payment.getFromCard());
        } else if (payment.getFromTin() != null) {
            payment.getFromTin().setStatus(TinStatus.SUSPICIOUS);
            tinRepository.save(payment.getFromTin());
        }
    }
}
