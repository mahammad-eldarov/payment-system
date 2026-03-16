package az.bank.paymentsystem.service;

import az.bank.paymentsystem.dto.response.StatusAuditLogResponse;
import az.bank.paymentsystem.entity.StatusAuditLogEntity;
import az.bank.paymentsystem.exception.EmptyListException;
import az.bank.paymentsystem.exception.PageRequestException;
import az.bank.paymentsystem.mapper.StatusAuditLogMapper;
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
    private final EntityFinderService entityFinderService;

    public Page<StatusAuditLogResponse> getCardHistory(Integer cardId, int page) {
        entityFinderService.findActiveCard(cardId);

        Pageable pageable = buildPageable(page);

        Page<StatusAuditLogEntity> logs = statusAuditLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc("CARD", cardId, pageable);

        if (logs.isEmpty()) throw new EmptyListException("No status changes found for this card.");


        return logs.map(statusAuditLogMapper::toResponse);

    }

    public Page<StatusAuditLogResponse> getAccountHistory(Integer accountId, int page) {
        entityFinderService.findActiveAccount(accountId);

        Pageable pageable = buildPageable(page);

        Page<StatusAuditLogEntity> logs = statusAuditLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc("ACCOUNT", accountId, pageable);

        if (logs.isEmpty()) throw new EmptyListException("No status changes found for this current account.");

        return logs.map(statusAuditLogMapper::toResponse);
    }

    public Page<StatusAuditLogResponse> getCustomerHistory(Integer customerId, int page) {
        entityFinderService.findActiveCustomer(customerId);

        Pageable pageable = buildPageable(page);

        Page<StatusAuditLogEntity> logs = statusAuditLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc("CUSTOMER", customerId, pageable);

        if (logs.isEmpty()) throw new EmptyListException("No status changes found for this customer.");

        return logs.map(statusAuditLogMapper::toResponse);

    }

    private Pageable buildPageable(int page) {
        if (page < 1) throw new PageRequestException("Page number must be at least 1");
        return PageRequest.of(page - 1, 10, Sort.by("createdAt").descending());
    }
}
