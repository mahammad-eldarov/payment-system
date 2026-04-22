package az.bank.paymentsystem.util.shared;

import az.bank.paymentsystem.entity.CardEntity;
import az.bank.paymentsystem.entity.TinEntity;
import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.entity.StatusAuditLogEntity;
import az.bank.paymentsystem.repository.StatusAuditLogRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StatusAuditLogger {

    private final StatusAuditLogRepository statusAuditLogRepository;

    public void log(String entityType, Integer entityId,
                    String previousStatus, String newStatus, String reason) {
        StatusAuditLogEntity log = new StatusAuditLogEntity();
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setPreviousStatus(previousStatus);
        log.setNewStatus(newStatus);
        log.setReason(reason);
        log.setCreatedAt(Instant.now());
        statusAuditLogRepository.save(log);
    }

    public void logCard(CardEntity card, String newStatus, String reason) {
        log("CARD", card.getId(), card.getStatus().name(), newStatus, reason);
    }

    public void logTin(TinEntity tin, String newStatus, String reason) {
        log("TIN", tin.getId(), tin.getStatus().name(), newStatus, reason);
    }

    public void logCustomer(CustomerEntity customer, String newStatus, String reason) {
        log("CUSTOMER", customer.getId(), customer.getStatus().name(), newStatus, reason);
    }
}
