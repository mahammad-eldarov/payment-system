package az.bank.paymentsystem.service;

import az.bank.paymentsystem.exception.PageRequestException;
import az.bank.paymentsystem.util.shared.CurrentAccountBalanceTransfer;
import az.bank.paymentsystem.util.shared.MessageUtil;
import az.bank.paymentsystem.util.shared.StatusAuditLogger;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import az.bank.paymentsystem.dto.response.MessageResponse;
import az.bank.paymentsystem.exception.AccountNotFoundException;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.exception.EmptyListException;
import az.bank.paymentsystem.entity.CurrentAccountEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.CurrentAccountStatus;
import az.bank.paymentsystem.dto.response.CurrentAccountResponse;
import az.bank.paymentsystem.mapper.CurrentAccountMapper;
import az.bank.paymentsystem.repository.CurrentAccountRepository;
import az.bank.paymentsystem.repository.CustomerRepository;
import az.bank.paymentsystem.util.currentAccount.CurrentAccountValidator;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final MessageSource messageSource;
    private final MessageUtil messageUtil;

    public List<CurrentAccountResponse> getAccountsByCustomerId(Integer id) {
        findActiveCustomer(id);
        Locale locale = LocaleContextHolder.getLocale();

        List<CurrentAccountEntity> accounts = currentAccountRepository.findByCustomerIdAndIsVisibleTrue(id);
        if (accounts.isEmpty()) {
            throw new EmptyListException(messageSource.getMessage("currentAccountService.getAccountsByCustomerId.customerNotHave", null, locale));
        }

        return accounts.stream().map(currentAccountMapper::toResponse).collect(Collectors.toList());
    }

    public CurrentAccountResponse getAccountByAccountNumber(String accountNumber) {

        return currentAccountMapper.toResponse(findActiveAccountByNumber(accountNumber));
    }


    public Page<CurrentAccountResponse> getCurrentAccountByStatus(CurrentAccountStatus status, int page) {
        Locale locale = LocaleContextHolder.getLocale();

        if (page < 1) throw new PageRequestException(messageSource.getMessage("statusAuditLogService.buildPageable.pageNumber", null, locale));

        Pageable pageable = PageRequest.of(page - 1, 10, Sort.by("createdAt").descending());

        Page<CurrentAccountEntity> accounts = currentAccountRepository.findByStatus(status, pageable);

        if (accounts.isEmpty()) {
            throw new AccountNotFoundException(messageSource.getMessage("currentAccountService.getCurrentAccountByStatus.currentAccountStatus", null, locale));
        }

        return accounts.map(currentAccountMapper::toResponse);
    }

    public MessageResponse updateCurrentAccountStatus(Integer id, CurrentAccountStatus status) {
        Locale locale = LocaleContextHolder.getLocale();

        CurrentAccountEntity account = findActiveAccount(id);
        statusAuditLogger.logAccount(account, status.name(), messageSource.getMessage("currentAccountService.updateCurrentAccountStatus.manualUpdateStatus", null, locale));
        account.setStatus(status);
        account.setUpdatedAt(Instant.now());
        currentAccountRepository.save(account);
        return new MessageResponse(messageSource.getMessage("currentAccountService.updateCurrentAccountStatus.updateResponse",null, locale));
    }


    public void updateExpiredCurrentAccounts() {
        List<CurrentAccountEntity> expiredAccounts = currentAccountRepository
                .findAllByExpiryDateLessThanEqualAndStatusNot(LocalDate.now(), CurrentAccountStatus.EXPIRED);

        expiredAccounts.forEach(account -> {
            Locale locale = messageUtil.resolveLocale(account.getCustomer());
            statusAuditLogger.logAccount(account, CurrentAccountStatus.EXPIRED.name(), messageSource.getMessage("currentAccountService.updateExpiredCurrentAccounts.accountExpiry", null, locale));
            account.setStatus(CurrentAccountStatus.EXPIRED);
            account.setUpdatedAt(Instant.now());
            currentAccountBalanceTransfer.transfer(account,locale);

        });

        currentAccountRepository.saveAll(expiredAccounts);
    }

    public MessageResponse deleteCurrentAccount(Integer id) {
        CurrentAccountEntity account = findActiveAccount(id);
        Locale locale = messageUtil.resolveLocale(account.getCustomer());
        currentAccountValidator.validateDeletion(account);
        statusAuditLogger.logAccount(account, CurrentAccountStatus.CLOSED.name(), messageSource.getMessage("currentAccountService.deleteCurrentAccount.accountClosed", null, locale));
        account.setStatus(CurrentAccountStatus.CLOSED);
        account.setIsVisible(false);
        account.setUpdatedAt(Instant.now());
        currentAccountRepository.save(account);
        return new MessageResponse(messageSource.getMessage("currentAccountService.deleteCurrentAccount.accountClosedSuccessfully", null, locale));
    }


    public CurrentAccountEntity findActiveAccount(Integer id) {
        Locale locale = LocaleContextHolder.getLocale();

        return currentAccountRepository.findByIdAndIsVisibleTrue(id)
                .orElseThrow(() -> new AccountNotFoundException(messageSource.getMessage("currentAccountService.findActiveAccount.accountNotFound", null, locale)));
    }

    public CurrentAccountEntity findActiveAccountByNumber(String accountNumber) {
        Locale locale = LocaleContextHolder.getLocale();

        return currentAccountRepository.findByAccountNumberAndIsVisibleTrue(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(messageSource.getMessage("currentAccountService.findActiveAccountByNumber.accountNotFound", null, locale)));
    }

    public CustomerEntity findActiveCustomer(Integer id) {
        Locale locale = LocaleContextHolder.getLocale();

        return customerRepository.findByIdAndIsVisibleTrue(id)
                .orElseThrow(() -> new CustomerNotFoundException(messageSource.getMessage("currentAccountService.findActiveCustomer.customerNotFound",null, locale)));
    }

}