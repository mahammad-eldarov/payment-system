package az.bank.paymentsystem.util.shared;

import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.CustomerStatus;
import az.bank.paymentsystem.exception.FraudDetectedException;
import az.bank.paymentsystem.config.BankConfig;
import az.bank.paymentsystem.repository.CustomerRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FraudDetectionChecker {

    private final CustomerRepository customerRepository;
    private final BankConfig bankConfig;

    public void checkDeletedSuspiciousCustomer(String pin) {
        Optional<CustomerEntity> deletedCustomer = customerRepository
                .findFirstByPinAndIsVisibleFalse(pin);

        if (deletedCustomer.isPresent()
                && deletedCustomer.get().getStatus() == CustomerStatus.SUSPICIOUS) {
            throw new FraudDetectedException(
                    "Profile creation denied. Previous profile with this PIN was flagged for suspicious activity. Please contact support!"
            );
        }
    }

    public void checkAccountCreationFrequency(String pin) {
        List<CustomerEntity> deletedCustomers = customerRepository
                .findAllByPinAndIsVisibleFalse(pin);

        if (deletedCustomers.size() >= bankConfig.getFraud().getMaxAccountCreations()) {
            throw new FraudDetectedException(
                    "Profile creation denied. Too many profile created with this PIN. Please contact support!"
            );
        }
    }
}
