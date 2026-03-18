package az.bank.paymentsystem.util.shared;

import az.bank.paymentsystem.entity.CustomerEntity;
import az.bank.paymentsystem.enums.CustomerStatus;
import az.bank.paymentsystem.exception.ExceptionResponse;
import az.bank.paymentsystem.repository.StatusAuditLogRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomerSuspiciousValidator {

    private final StatusAuditLogRepository statusAuditLogRepository;

    public void validate(CustomerEntity customer, List<ExceptionResponse> errors) {

        // Customer hazırda suspicious-dursa
        if (customer.getStatus() == CustomerStatus.SUSPICIOUS) {
            errors.add(new ExceptionResponse(
                    403,
                    "Profile is suspended due to suspicious activity.",
                    LocalDateTime.now()
            ));
        }

        // Customer əvvəllər suspicious olubsa → permanent block
        int suspiciousCount = statusAuditLogRepository
                .countByEntityTypeAndEntityIdAndNewStatus(
                        "CUSTOMER",
                        customer.getId(),
                        "SUSPICIOUS"
                );

        if (suspiciousCount >= 1) {
            errors.add(new ExceptionResponse(
                    403,
                    "Your profile has been permanently blocked due to repeated suspicious activity.",
                    LocalDateTime.now()
            ));
        }
    }
}