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
import lombok.RequiredArgsConstructor;
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

    public Page<StatusAuditLogResponse> getCardHistory(Integer cardId, int page) {
        findActiveCard(cardId);

        Pageable pageable = buildPageable(page);

        Page<StatusAuditLogEntity> logs = statusAuditLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc("CARD", cardId, pageable);

        if (logs.isEmpty()) throw new EmptyListException("statusAuditLogService.getCardHistory.cardStatusChanged");


        return logs.map(statusAuditLogMapper::toResponse);

    }

    public Page<StatusAuditLogResponse> getAccountHistory(Integer accountId, int page) {
        findActiveAccount(accountId);

        Pageable pageable = buildPageable(page);

        Page<StatusAuditLogEntity> logs = statusAuditLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc("ACCOUNT", accountId, pageable);

        if (logs.isEmpty()) throw new EmptyListException("statusAuditLogService.getAccountHistory.accountStatusChanged");

        return logs.map(statusAuditLogMapper::toResponse);
    }

    public Page<StatusAuditLogResponse> getCustomerHistory(Integer customerId, int page) {
        findActiveCustomer(customerId);

        Pageable pageable = buildPageable(page);

        Page<StatusAuditLogEntity> logs = statusAuditLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc("CUSTOMER", customerId, pageable);

        if (logs.isEmpty()) throw new EmptyListException("statusAuditLogService.getCustomerHistory.customerStatusChanged");

        return logs.map(statusAuditLogMapper::toResponse);

    }

    private Pageable buildPageable(int page) {
        if (page < 1) throw new PageRequestException("statusAuditLogService.buildPageable.pageNumber");
        return PageRequest.of(page - 1, 10, Sort.by("createdAt").descending());
    }

    public void findActiveCard(Integer id) {
        cardRepository.findByIdAndIsVisibleTrue(id)
                .orElseThrow(() -> new CardNotFoundException("statusAuditLogService.findActiveCard.cardNotFound"));
    }

    public void findActiveCustomer(Integer id) {
        customerRepository.findByIdAndIsVisibleTrue(id)
                .orElseThrow(() -> new CustomerNotFoundException("statusAuditLogService.findActiveCustomer.customerNotFound"));
    }

    public void findActiveAccount(Integer id) {
        currentAccountRepository.findByIdAndIsVisibleTrue(id)
                .orElseThrow(() -> new AccountNotFoundException("statusAuditLogService.findActiveAccount.accountNotFound"));
    }
}
