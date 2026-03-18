package az.bank.paymentsystem.util.currentAccount;

import az.bank.paymentsystem.entity.CurrentAccountEntity;
import az.bank.paymentsystem.entity.CurrentAccountOrderEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.CurrentAccountStatus;
import az.bank.paymentsystem.enums.CustomerStatus;
import az.bank.paymentsystem.enums.OrderStatus;
import az.bank.paymentsystem.exception.AccountAlreadyCancelledException;
import az.bank.paymentsystem.exception.AccountExpiredException;
import az.bank.paymentsystem.exception.AccountLimitExceededException;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.exception.CustomerSuspiciousException;
import az.bank.paymentsystem.repository.CurrentAccountRepository;
import az.bank.paymentsystem.repository.CustomerRepository;
//import az.bank.paymentsystem.service.EntityFinderService;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentAccountValidator {

    private final CustomerRepository customerRepository;
    private final CurrentAccountRepository currentAccountRepository;
//    private final EntityFinderService entityFinderService;
    private final CurrentAccountCreator currentAccountCreator;

    public void process(CurrentAccountOrderEntity request) {
        List<String> reasons = new ArrayList<>();
        CustomerEntity customer = request.getCustomer();

        if (customer.getStatus() == CustomerStatus.SUSPICIOUS) {
            reasons.add("Customer is suspended due to suspicious activity.");
        }
        if (currentAccountRepository.countByCustomerIdAndIsVisibleTrue(customer.getId()) >= 3) {
            reasons.add("Current account limit exceeded.");
        }

        if (!reasons.isEmpty()) {
            request.setStatus(OrderStatus.REJECTED);
            request.setRejectionReason(String.join(", ", reasons));
        } else {
            CurrentAccountEntity account = currentAccountCreator.createOrderAccount(request);
            currentAccountRepository.save(account);
            request.setStatus(OrderStatus.APPROVED);
            request.setUpdatedAt(Instant.now());
        }
    }

    public void validateDeletion(CurrentAccountEntity account) {
        if (account.getStatus() == CurrentAccountStatus.CLOSED) {
            throw new AccountAlreadyCancelledException("The current account has already been canceled.");
        }
        if (account.getStatus() == CurrentAccountStatus.EXPIRED) {
            throw new AccountExpiredException("An expired current account cannot be canceled.");
        }
    }

    public void validateAccountOrder(Integer customerId, Integer accountCount) {

        CustomerEntity customer = customerRepository.findByIdAndIsVisibleTrue(customerId)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
//        CustomerEntity customer = entityFinderService.findActiveCustomer(customerId);

        if (customer.getStatus() == CustomerStatus.SUSPICIOUS) {
            throw new CustomerSuspiciousException("Your account is suspended due to suspicious activity.");
        }

        if (accountCount >= 3) {
            throw new AccountLimitExceededException(
                    "The customer already has 3 current accounts. A new account cannot be ordered.");
        }
    }
}
