package az.bank.paymentsystem.service;

import az.bank.paymentsystem.dto.response.StatusAuditLogResponse;
import az.bank.paymentsystem.entity.StatusAuditLogEntity;
import az.bank.paymentsystem.exception.AccountNotFoundException;
import az.bank.paymentsystem.exception.CardNotFoundException;
import az.bank.paymentsystem.exception.CustomerNotFoundException;
import az.bank.paymentsystem.exception.EmptyListException;
import az.bank.paymentsystem.exception.PageRequestException;
import az.bank.paymentsystem.mapper.StatusAuditLogMapper;
import az.bank.paymentsystem.repository.CardRepository;
import az.bank.paymentsystem.repository.CurrentAccountRepository;
import az.bank.paymentsystem.repository.CustomerRepository;
import az.bank.paymentsystem.repository.StatusAuditLogRepository;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatusAuditLogService {

    private final StatusAuditLogRepository statusAuditLogRepository;
    private final StatusAuditLogMapper statusAuditLogMapper;
    private final CardRepository cardRepository;
    private final CurrentAccountRepository currentAccountRepository;
    private final CustomerRepository customerRepository;
    private final MessageSource messageSource;

    public Page<StatusAuditLogResponse> getCardHistory(Integer cardId, int page) {
        Locale locale = LocaleContextHolder.getLocale();
        findActiveCard(cardId);

        Pageable pageable = buildPageable(page);

        Page<StatusAuditLogEntity> logs = statusAuditLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc("CARD", cardId, pageable);

        if (logs.isEmpty()) throw new EmptyListException(messageSource.getMessage("statusAuditLogService.getCardHistory.cardStatusChanged",null,locale));


        return logs.map(statusAuditLogMapper::toResponse);

    }

    public Page<StatusAuditLogResponse> getAccountHistory(Integer accountId, int page) {
        Locale locale = LocaleContextHolder.getLocale();
        findActiveAccount(accountId);

        Pageable pageable = buildPageable(page);

        Page<StatusAuditLogEntity> logs = statusAuditLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc("ACCOUNT", accountId, pageable);

        if (logs.isEmpty()) throw new EmptyListException(messageSource.getMessage("statusAuditLogService.getAccountHistory.accountStatusChanged",null,locale));

        return logs.map(statusAuditLogMapper::toResponse);
    }

    public Page<StatusAuditLogResponse> getCustomerHistory(Integer customerId, int page) {
        Locale locale = LocaleContextHolder.getLocale();
        findActiveCustomer(customerId);

        Pageable pageable = buildPageable(page);

        Page<StatusAuditLogEntity> logs = statusAuditLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc("CUSTOMER", customerId, pageable);

        if (logs.isEmpty()) throw new EmptyListException(messageSource.getMessage("statusAuditLogService.getCustomerHistory.customerStatusChanged",null,locale));

        return logs.map(statusAuditLogMapper::toResponse);

    }

    private Pageable buildPageable(int page) {
        Locale locale = LocaleContextHolder.getLocale();
        if (page < 1) throw new PageRequestException(messageSource.getMessage("statusAuditLogService.buildPageable.pageNumber",null,locale));
        return PageRequest.of(page - 1, 10, Sort.by("createdAt").descending());
    }

    public void findActiveCard(Integer id) {
        Locale locale = LocaleContextHolder.getLocale();
        cardRepository.findByIdAndIsVisibleTrue(id)
                .orElseThrow(() -> new CardNotFoundException(messageSource.getMessage("statusAuditLogService.findActiveCard.cardNotFound",null,locale)));
    }

    public void findActiveCustomer(Integer id) {
        Locale locale = LocaleContextHolder.getLocale();
        customerRepository.findByIdAndIsVisibleTrue(id)
                .orElseThrow(() -> new CustomerNotFoundException(messageSource.getMessage("statusAuditLogService.findActiveCustomer.customerNotFound",null,locale)));
    }

    public void findActiveAccount(Integer id) {
        Locale locale = LocaleContextHolder.getLocale();
        currentAccountRepository.findByIdAndIsVisibleTrue(id)
                .orElseThrow(() -> new AccountNotFoundException(messageSource.getMessage("statusAuditLogService.findActiveAccount.accountNotFound",null,locale)));
    }
}
