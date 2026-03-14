package az.bank.paymentsystem.util.currentAccount;

import az.bank.paymentsystem.entity.CurrentAccountEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.CurrentAccountStatus;
import az.bank.paymentsystem.enums.CustomerStatus;
import az.bank.paymentsystem.exception.AccountAlreadyCancelledException;
import az.bank.paymentsystem.exception.AccountExpiredException;
import az.bank.paymentsystem.exception.AccountLimitExceededException;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.exception.CustomerSuspiciousException;
import az.bank.paymentsystem.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentAccountValidator {

    private final CustomerRepository customerRepository;

    public void validateDeletion(CurrentAccountEntity account) {
        if (account.getStatus() == CurrentAccountStatus.CLOSED) {
            throw new AccountAlreadyCancelledException("The current account has already been canceled.");
        }
        if (account.getStatus() == CurrentAccountStatus.EXPIRED) {
            throw new AccountExpiredException("An expired current account cannot be canceled.");
        }
    }

    public void validateAccountOrder(Integer customerId, int accountCount) {

        CustomerEntity customer = customerRepository.findByIdAndIsVisibleTrue(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));

        if (customer.getStatus() == CustomerStatus.SUSPICIOUS) {
            throw new CustomerSuspiciousException("Your account is suspended due to suspicious activity.");
        }

        if (accountCount >= 3) {
            throw new AccountLimitExceededException(
                    "The customer already has 3 current accounts. A new account cannot be ordered.");
        }
    }
}
