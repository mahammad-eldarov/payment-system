package az.bank.paymentsystem.repository;

import az.bank.paymentsystem.entity.StatusAuditLogEntity;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusAuditLogRepository extends JpaRepository <StatusAuditLogEntity, Integer> {
//    List<StatusAuditLogEntity> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, Integer entityId);
    Page<StatusAuditLogEntity> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            String entityType, Integer entityId, Pageable pageable);

    Integer countByEntityTypeAndEntityIdAndNewStatus(
            String entityType,
            Integer entityId,
            String newStatus
    );
}
