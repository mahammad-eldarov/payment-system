package az.bank.paymentsystem.service;

import az.bank.paymentsystem.dto.response.StatusAuditLogResponse;
import az.bank.paymentsystem.entity.StatusAuditLogEntity;
import az.bank.paymentsystem.exception.EmptyListException;
import az.bank.paymentsystem.mapper.StatusAuditLogMapper;
import az.bank.paymentsystem.repository.StatusAuditLogRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StatusAuditLogService {

    private final StatusAuditLogRepository statusAuditLogRepository;
    private final StatusAuditLogMapper statusAuditLogMapper;
    private final EntityFinderService entityFinderService;

    public List<StatusAuditLogResponse> getCardHistory(Integer cardId) {
        entityFinderService.findActiveCard(cardId);

        List<StatusAuditLogEntity> logs = statusAuditLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc("CARD", cardId);

        if (logs.isEmpty()) throw new EmptyListException("No status changes found for this card.");

        return logs.stream().map(statusAuditLogMapper::toResponse).toList();

//        return statusAuditLogRepository
//                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc("CARD", cardId)
//                .stream().map(statusAuditLogMapper::toResponse).toList();
    }

    public List<StatusAuditLogResponse> getAccountHistory(Integer accountId) {
        entityFinderService.findActiveAccount(accountId);

        List<StatusAuditLogEntity> logs = statusAuditLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc("ACCOUNT", accountId);

        if (logs.isEmpty()) throw new EmptyListException("No status changes found for this current account.");

        return logs.stream().map(statusAuditLogMapper::toResponse).toList();

//        return statusAuditLogRepository
//                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc("ACCOUNT", accountId)
//                .stream().map(statusAuditLogMapper::toResponse).toList();
    }

    public List<StatusAuditLogResponse> getCustomerHistory(Integer customerId) {
        entityFinderService.findActiveCustomer(customerId);

        List<StatusAuditLogEntity> logs = statusAuditLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc("CUSTOMER", customerId);

        if (logs.isEmpty()) throw new EmptyListException("No status changes found for this customer.");

        return logs.stream().map(statusAuditLogMapper::toResponse).toList();


//        return statusAuditLogRepository
//                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc("CUSTOMER", customerId)
//                .stream().map(statusAuditLogMapper::toResponse).toList();
    }
}
