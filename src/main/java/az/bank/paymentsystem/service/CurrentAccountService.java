package az.bank.paymentsystem.service;

import az.bank.paymentsystem.exception.OperationNotAllowedException;
import az.bank.paymentsystem.util.shared.CurrentAccountBalanceTransfer;
import az.bank.paymentsystem.util.shared.StatusAuditLogger;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.dto.response.MessageResponse;
import az.bank.paymentsystem.exception.AccountNotFoundException;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.exception.EmptyListException;
import az.bank.paymentsystem.entity.CurrentAccountEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.CurrentAccountStatus;
import az.bank.paymentsystem.dto.request.OrderCurrentAccountRequest;
import az.bank.paymentsystem.dto.response.CurrentAccountResponse;
import az.bank.paymentsystem.mapper.CurrentAccountMapper;
import az.bank.paymentsystem.repository.CurrentAccountRepository;
import az.bank.paymentsystem.repository.CustomerRepository;
import az.bank.paymentsystem.util.currentAccount.CurrentAccountCreator;
import az.bank.paymentsystem.util.currentAccount.CurrentAccountValidator;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrentAccountService {

    private final CurrentAccountRepository currentAccountRepository;
    private final CustomerRepository customerRepository;
    private final CurrentAccountMapper currentAccountMapper;
    private final CurrentAccountValidator currentAccountValidator;
    private final CurrentAccountBalanceTransfer currentAccountBalanceTransfer;
    private final StatusAuditLogger statusAuditLogger;

    //GET
    public List<CurrentAccountResponse> getAccountsByCustomerId(Integer id) {
        findActiveCustomer(id);

        List<CurrentAccountEntity> accounts = currentAccountRepository.findByCustomerIdAndIsVisibleTrue(id);
        if (accounts.isEmpty()) {
            throw new EmptyListException("This customer does not have any current accounts.");
        }

        return accounts.stream().map(currentAccountMapper::toResponse).collect(Collectors.toList());
    }

    public CurrentAccountResponse getAccountByAccountNumber(String accountNumber) {
        return currentAccountMapper.toResponse(findActiveAccountByNumber(accountNumber));
    }

    public List<CurrentAccountResponse> getCurrentAccountByStatus(CurrentAccountStatus status) {

        List<CurrentAccountEntity> accounts = currentAccountRepository.findByStatusAndIsVisibleTrue(status);
        if (accounts.isEmpty()) {
            throw new AccountNotFoundException("No current account found with this status");
        }

        return accounts.stream().map(currentAccountMapper::toResponse).collect(Collectors.toList());
    }

    public MessageResponse updateCurrentAccountStatus(Integer id, CurrentAccountStatus status) {
        CurrentAccountEntity account = findActiveAccount(id);
        statusAuditLogger.logAccount(account, status.name(), "Status updated manually");
        account.setStatus(status);
        account.setUpdatedAt(Instant.now());
        currentAccountRepository.save(account);
        return new MessageResponse("Current account status updated successfully");
    }


    public void updateExpiredCurrentAccounts() {
        List<CurrentAccountEntity> expiredAccounts = currentAccountRepository
                .findAllByExpiryDateLessThanEqualAndStatusNot(LocalDate.now(), CurrentAccountStatus.EXPIRED);

        expiredAccounts.forEach(account -> {
            statusAuditLogger.logAccount(account, CurrentAccountStatus.EXPIRED.name(), "Account expiry date reached");
            account.setStatus(CurrentAccountStatus.EXPIRED);
            account.setUpdatedAt(Instant.now());
            currentAccountBalanceTransfer.transfer(account);
        });

        currentAccountRepository.saveAll(expiredAccounts);
    }

    // DELETE
    public MessageResponse deleteCurrentAccount(Integer id) {
        CurrentAccountEntity account = findActiveAccount(id);
        currentAccountValidator.validateDeletion(account);
        statusAuditLogger.logAccount(account, CurrentAccountStatus.CLOSED.name(), "Current account closed by customer");
        account.setStatus(CurrentAccountStatus.CLOSED);
        account.setIsVisible(false);
        account.setUpdatedAt(Instant.now());
        currentAccountRepository.save(account);
        return new MessageResponse("Current account was successfully deleted.");
    }


    public CurrentAccountEntity findActiveAccount(Integer id) {
        return currentAccountRepository.findByIdAndIsVisibleTrue(id)
                .orElseThrow(() -> new AccountNotFoundException("Current account not found"));
    }

    public CurrentAccountEntity findActiveAccountByNumber(String accountNumber) {
        return currentAccountRepository.findByAccountNumberAndIsVisibleTrue(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Current account not found"));
    }

    public CustomerEntity findActiveCustomer(Integer id) {
        return customerRepository.findByIdAndIsVisibleTrue(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found"));
    }

}