package az.bank.paymentsystem.util.shared;

import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.CustomerStatus;
import az.bank.paymentsystem.exception.FraudDetectedException;
import az.bank.paymentsystem.config.BankConfig;
import az.bank.paymentsystem.repository.CustomerRepository;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FraudDetectionChecker {

    private final CustomerRepository customerRepository;
    private final BankConfig bankConfig;
    private final MessageSource messageSource;

    public void checkDeletedSuspiciousCustomer(String pin) {
        Locale locale = LocaleContextHolder.getLocale();
        Optional<CustomerEntity> deletedCustomer = customerRepository
                .findFirstByPinAndIsVisibleFalse(pin);

        if (deletedCustomer.isPresent()
                && deletedCustomer.get().getStatus() == CustomerStatus.SUSPICIOUS) {
            throw new FraudDetectedException(
                    messageSource.getMessage("fraudDetectionChecker.checkDeletedSuspiciousCustomer",null, locale)
            );
        }
    }

    public void checkAccountCreationFrequency(String pin) {
        Locale locale = LocaleContextHolder.getLocale();
        List<CustomerEntity> deletedCustomers = customerRepository
                .findAllByPinAndIsVisibleFalse(pin);

        if (deletedCustomers.size() >= bankConfig.getFraud().getMaxAccountCreations()) {
            throw new FraudDetectedException(
                    messageSource.getMessage("fraudDetectionChecker.checkAccountCreationFrequency",null, locale)
            );
        }
    }
}
