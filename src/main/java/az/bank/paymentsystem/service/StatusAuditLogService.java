package az.bank.paymentsystem.service;

import az.bank.paymentsystem.dto.response.StatusAuditLogResponse;
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

    public List<StatusAuditLogResponse> getCardHistory(Integer cardId) {
        return statusAuditLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc("CARD", cardId)
                .stream().map(statusAuditLogMapper::toResponse).toList();
    }

    public List<StatusAuditLogResponse> getAccountHistory(Integer accountId) {
        return statusAuditLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc("ACCOUNT", accountId)
                .stream().map(statusAuditLogMapper::toResponse).toList();
    }

    public List<StatusAuditLogResponse> getCustomerHistory(Integer customerId) {
        return statusAuditLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc("CUSTOMER", customerId)
                .stream().map(statusAuditLogMapper::toResponse).toList();
    }
}
