package az.bank.paymentsystem.dto.response;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatusAuditLogResponse {
    private Integer id;
    private String entityType;
    private Integer entityId;
    private String previousStatus;
    private String newStatus;
    private String reason;
    private Instant createdAt;
}