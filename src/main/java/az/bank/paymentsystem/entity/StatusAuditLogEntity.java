package az.bank.paymentsystem.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "status_audit_log")
public class StatusAuditLogEntity extends BaseEntity {

    @Column(name = "entity_type", length = Integer.MAX_VALUE)
    private String entityType;

    @Column(name = "entity_id")
    private Integer entityId;

    @Column(name = "previous_status", length = Integer.MAX_VALUE)
    private String previousStatus;

    @Column(name = "new_status", length = Integer.MAX_VALUE)
    private String newStatus;

    @Column(name = "reason", length = Integer.MAX_VALUE)
    private String reason;


}